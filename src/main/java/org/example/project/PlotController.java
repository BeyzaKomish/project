package org.example.project;

import org.example.project.Repository.DataRepo;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;

@Controller
public class PlotController {

     // interface for fetching the interface responsible for fetching the data from mongoDB
    @Autowired
    private DataRepo dataRepo;

    @Autowired
    private org.graalvm.polyglot.Value plotFunction;

    // sets up the execution enviroment
    @Autowired
    private Context graalContext;

    // we are storing the entire 100 rows in this DataHolder
    // so that the application doesnt query the db every refresh
    private static List<DataHolder> allData = null;

    // this index keeps track of which row to point to send to R
    // ensuring parallel stream of data
    private static int currentDataIndex = 0;
    // a window size for the expanding graph
    private static final int PLOT_WINDOW_SIZE = 100;

    @Bean
    public Context getGraalVMContext() {
        return Context.newBuilder().allowAllAccess(true).build();
    }

    // this over here loads the plot.R script from the classspath
    // returns the main plot function
    @Bean
    public static org.graalvm.polyglot.Value getPlotFunction(@Autowired Context ctx, @Value("classpath:plot.R") Resource rSource)
            throws IOException {
        Source source = Source.newBuilder("R", rSource.getURL()).build();
        // This evaluates the R script and returns the main function block.
        return ctx.eval(source);
    }

   // maps to the plot URl in browser
   // tells the browser that the response is in SVG image example
    @RequestMapping(value = "/plot", produces = "image/svg+xml")
    public ResponseEntity<String> load() {

        synchronized (PlotController.class) {

            // at first the list is null so the below code runs
            // allows us to fetch all the data in mycollection and store them in alldata
            if (allData == null) {
                allData = dataRepo.findAll();
                //checks once again to be sure but usually skipped cause we now have all the data
                if (allData == null || allData.isEmpty()) {
                    return new ResponseEntity<>("Error: No data available from repository.", HttpStatus.BAD_REQUEST);
                }
            }
            // checks to see if the data stream finished to reset the graph by using the reset state function from R file
            // skipped otherwise
            if (currentDataIndex >= allData.size()) {
                currentDataIndex = 0;

                org.graalvm.polyglot.Value rBindings = graalContext.getBindings("R");
                org.graalvm.polyglot.Value resetFunction = rBindings.getMember("reset_state");

                if (resetFunction != null && resetFunction.canExecute()) {
                    resetFunction.execute();
                }
            }

            // retieves the next data point
            // fethces one data at a time
            // increments index for the x axis to poin the y value to
            double nextYValue = allData.get(currentDataIndex).getValue();
            currentDataIndex++;

            HttpHeaders responseHeaders = new HttpHeaders();
            //instructs browser to automatically reload page for dymanic effect
            responseHeaders.set("Refresh", "1");

            String svg;
            // executes R function and passes the values and the window size
            org.graalvm.polyglot.Value result = plotFunction.execute(nextYValue, PLOT_WINDOW_SIZE);
            svg = result.asString();// finally resulşting from R wrapped string and returned as a web respones

            return new ResponseEntity<>(svg, responseHeaders, HttpStatus.OK);
        }
    }
}
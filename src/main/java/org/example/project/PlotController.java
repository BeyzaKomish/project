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

    @Autowired
    private DataRepo dataRepo;

    // Use fully qualified name to avoid conflict with Spring's @Value
    @Autowired
    private org.graalvm.polyglot.Value plotFunction;

    // 🚩 Injected Context field for use in the load() method
    @Autowired
    private Context graalContext;

    // --- STATIC STATE VARIABLES ---
    private static List<DataHolder> allData = null;
    private static int currentDataIndex = 0;
    private static final int PLOT_WINDOW_SIZE = 100;

    // --- BEAN DEFINITIONS ---

    @Bean
    public Context getGraalVMContext() {
        return Context.newBuilder().allowAllAccess(true).build();
    }

    // 🚩 FIX 1: Declaring the factory method as static to break the circular reference.
    // We inject the Resource and Context directly into the static method.
    @Bean
    public static org.graalvm.polyglot.Value getPlotFunction(@Autowired Context ctx, @Value("classpath:plot.R") Resource rSource)
            throws IOException {
        Source source = Source.newBuilder("R", rSource.getURL()).build();
        // This evaluates the R script and returns the main function block.
        return ctx.eval(source);
    }

    // --- CONTROLLER METHOD ---
    // ... (The load() method logic remains the same as the previous correct version) ...
    @RequestMapping(value = "/plot", produces = "image/svg+xml")
    public ResponseEntity<String> load() {

        synchronized (PlotController.class) {

            if (allData == null) {
                allData = dataRepo.findAll();
                if (allData == null || allData.isEmpty()) {
                    return new ResponseEntity<>("Error: No data available from repository.", HttpStatus.BAD_REQUEST);
                }
            }

            if (currentDataIndex >= allData.size()) {
                currentDataIndex = 0;

                org.graalvm.polyglot.Value rBindings = graalContext.getBindings("R");
                org.graalvm.polyglot.Value resetFunction = rBindings.getMember("reset_state");

                if (resetFunction != null && resetFunction.canExecute()) {
                    resetFunction.execute();
                }
            }

            double nextYValue = allData.get(currentDataIndex).getValue();
            currentDataIndex++;

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Refresh", "1");

            String svg;
            org.graalvm.polyglot.Value result = plotFunction.execute(nextYValue, PLOT_WINDOW_SIZE);
            svg = result.asString();

            return new ResponseEntity<>(svg, responseHeaders, HttpStatus.OK);
        }
    }
}
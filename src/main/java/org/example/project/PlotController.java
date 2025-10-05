package org.example.project;

import org.example.project.Repository.DataRepo;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
// NOTE: Removed 'import org.graalvm.polyglot.Value;' to resolve conflict

import org.springframework.beans.factory.annotation.Value; // Spring's @Value is needed
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

@Controller
public class PlotController {

    @Autowired
    private DataRepo dataRepo;

    // This uses Spring's @Value
    @Value("classpath:plot.R")
    private Resource resource;

    // Use the Context object directly
    @Autowired
    private Context graalContext;

    // --- BEAN DEFINITIONS ---

    @Bean
    public Context getGraalVMContext() {
        return Context.newBuilder().allowAllAccess(true).build();
    }

    // --- CONTROLLER METHOD ---

    @RequestMapping(value = "/plot", produces = "image/svg+xml")
    public ResponseEntity<String> load() throws IOException {

        // Ensure R script is loaded before execution
        Source source = Source.newBuilder("R", resource.getURL()).build();
        graalContext.eval(source);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Refresh", "1");

        // data fetching (assuming DataHolder and DataRepo are defined elsewhere)
        List<DataHolder> data = dataRepo.findAll();

        if (data == null || data.isEmpty()) {
            return new ResponseEntity<>("No data available to plot", HttpStatus.BAD_REQUEST);
        }

        // Data Preparation: X and Y arrays
        double[] yValues = data.stream().mapToDouble(DataHolder::getValue).toArray();
        int[] xValues = IntStream.rangeClosed(1, yValues.length).toArray();

        // --- R Execution: The Fix ---
        String svg;

        // FIX 1: Use fully qualified name for GraalVM Value
        org.graalvm.polyglot.Value rPlotFunction = graalContext.getBindings("R").getMember("plotData");

        synchronized(rPlotFunction){
            // FIX 2: Use fully qualified name for GraalVM Value
            org.graalvm.polyglot.Value result = rPlotFunction.execute(xValues, yValues);
            svg = result.asString();
        }

        return new ResponseEntity<>(svg, responseHeaders, HttpStatus.OK);
    }
}
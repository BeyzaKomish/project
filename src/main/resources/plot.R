# The function MUST be named 'plotData' to match the Java polyglotContext.getMember("plotData") call
plotData <- function(x_data, y_data) {

    # Extracting from an array/list is NO LONGER NECESSARY.
    # The arguments are now directly x_data and y_data.

    x <- x_data
    y <- y_data

    # Ensure numeric vectors
    x <- as.numeric(x)
    y <- as.numeric(y)

    # Check for equal length (important!)
    if (length(x) != length(y)) {
        stop("X and Y vectors must be of equal length.")
    }

    # ... (rest of the plotting and file reading logic) ...

    # Make plot
    svg("plot.svg", width=6, height=4)
    plot(x, y, type="l", col="darkorange", xlab="Index", ylab="Value", main="CSV Column Plot")
    dev.off()

    # Read back SVG content
    svg_content <- paste(readLines("plot.svg"), collapse="\n")
    return(svg_content)
}
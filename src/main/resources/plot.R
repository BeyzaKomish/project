# plot.R
library(lattice)

# 1. INITIALIZE GLOBAL STATE VARIABLES FIRST AND SIMPLY
data_history <<- numeric(0)
row_numbers <<- numeric(0)
global_row_index <<- 0

# 2. DEFINITION: Reset function
reset_state <- function() {
    data_history <<- numeric(0)
    row_numbers <<- numeric(0)
    global_row_index <<- 0
}

# 3. DEFINITION: The main plotting function (must be the last expression returned)
main_plot_function <- function(newValue, windowSize) {

    # Check if variables exist; if not, re-initialize (Safety Check)
    if (!exists("data_history")) {
        data_history <<- numeric(0)
        row_numbers <<- numeric(0)
        global_row_index <<- 0
    }

    # 1. Update counters and history
    global_row_index <<- global_row_index + 1
    data_history <<- c(data_history, newValue)
    row_numbers <<- c(row_numbers, global_row_index)

    # 2. Trim the data if it exceeds the window size (100)
    current_size <- length(data_history)
    if (current_size > windowSize) {
        data_history <<- data_history[(current_size - windowSize + 1):current_size]
        row_numbers <<- row_numbers[(current_size - windowSize + 1):current_size]
    }

    # 3. Calculate dynamic plot range for Y-axis (Robustness fix from before)
    if (current_size == 0) {
        # Should not happen after the first point, but for safety
        y_range <- c(-1, 1)
    } else if (current_size == 1) {
        val <- data_history[1]
        buffer <- max(0.05 * abs(val), 0.05)
        y_range <- c(val - buffer, val + buffer)
    } else {
        y_range <- range(data_history, na.rm = TRUE)
        y_diff <- y_range[2] - y_range[1]
        y_buffer <- max(0.01 * y_diff, 0.05)
        y_range <- c(y_range[1] - y_buffer, y_range[2] + y_buffer)
    }

    # 4. Generate the plot as SVG
    svg()

    plot <- xyplot(Value ~ RowNumber,
       data=data.frame(Value = data_history, RowNumber = row_numbers),
       main='Dynamic Expanding Plot (Capped at 100 Rows)',
       ylab="Data Value",
       xlab="Row Number (X-Axis)",
       type = c('l', 'g'),
       ylim = y_range,
       col.line='dark blue')

    print(plot)

    svg.off()
}

# 4. Return the main function as the final expression of the script.
main_plot_function
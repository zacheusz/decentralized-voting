set term epslatex standalone color linewidth 1.25
set output "baseline_std.tex"
set key left
set key top
set xlabel "$\\alpha$"
#set ylabel "score"
set xrange [.45:1]
set yrange [-5:110]
plot 110*(2*x-1) title "expected tally" with lines lt 0 lw 4,\
     0		 title ""	 	with lines lt 0 lw 4,\
     "baseline.data" using 1:(110*(2*$1-1)):2 title "standard deviation" with errorbars lt 1 lw 2

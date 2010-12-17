set term epslatex standalone color linewidth 1.25
set output "baseline_prop.tex"
set key right center
set xlabel "$\\alpha$"
#set ylabel "proportion"
set xrange [.45:1]
set yrange [0:110]
plot 100	     	       title "" with lines lt 0 lw 4,\
     "baseline.data" using 1:4 title "valid output" with linesp lt 3 pt 3 lw 2,\
     "baseline.data" using 1:3 title "null output" with linesp lt 1 pt 1 lw 2

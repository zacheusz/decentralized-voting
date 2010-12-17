#!/bin/bash
reset
set term epslatex standalone linewidth 1.5 font 'ptm,m,n' `echo $SIZE`
set output "maliciousk2.tex"

N=400 # nb of nodes
m=20 # nb of malicious nodes

#regression
average(x)=a*(x-0.5)+b
#N*(2*x-1)-m*(4*2) title "malicious tally" with lines lt 0
fit average(x) "maliciousk2.data" using ($8/1000):3 via a,b

average_expected(x)=760*(x-0.5)-160

expected(x)=N*(2*x-1)
bound(x)=N*(2*x-1)-m*(6*2+2)

width=0.9
heightTOP=0.35
heightBOTTOM=1-heightTOP

#TOP GRAPH
set lmargin 10
set rmargin 2
set multiplot
set origin 0.0,heightBOTTOM
set size width,heightTOP
set key top left

set x2tics 0.1
set xtics (0)
set yrange [0:1.1]
set xrange [.5:1]
set bmargin 0
set ytics nomirror
set ylabel "Fraction of nodes"

plot "maliciousk2.data" using ($8/1000):($7/100) title "" with points pt 2 lc 1 ps 0.8

#BOTTOM GRAPH
reset
set lmargin 10
set rmargin 2

set tmargin 0
set origin 0.0,0.0
set size width,heightBOTTOM
set key left top
set xlabel "$\\alpha$"
set xtics nomirror
set xrange [.5:1]
set yrange [-20:399]
set ytics nomirror
set ylabel "Tally"

# 400*(2*x-1)-m*(4*2+2*x) title "malicious tally" with lines lt 0,\

#Arrow between expected and bound
topToBottomAt=0.99
set label "$6k+2$" at topToBottomAt-0.06,(expected(topToBottomAt)+bound(topToBottomAt))/2
set arrow 1 from topToBottomAt,expected(topToBottomAt) to topToBottomAt,bound(topToBottomAt)
set arrow 2 from topToBottomAt,bound(topToBottomAt) to topToBottomAt,expected(topToBottomAt)


plot -1000 title "Valid binary output" with points pt 2 lt 1,\
     average(x) title "" ls 1,\
     "maliciousk2.data" using ($8/1000):3 title "Average outcome" with points pt 8 ps 0.8 lc 1,\
     0 with lines ls 1 title "",\
     expected(x) title "Expected outcome" with lines ls 2,\
     bound(x) title "" with lines lt 0
set nomultiplot
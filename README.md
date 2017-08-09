# apacheAccessLogAnalysis
Analysis of an Apache Access Log File

Log has to be configured in its default way

- LogFormat "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"" combined
- LogFormat "%h %l %u %t \"%r\" %>s %b" common

It uses JFreeChart library http://www.jfree.org/jfreechart/
---------------
Herramienta de análisis del fichero de logs de acceso de apache.

Más información en http://miguelmerelo.es/accesoapache/

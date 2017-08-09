package analizar_apache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class http_log_analisis {
	
	static int TIEMPO_ENTRE_CONEXIONES = 3600; //en segundos, tiempo entre conexiones de un host para que cuente como nueva conexion
	
	/**
	 * Método principal del programa
	 * @param args Argumentos por línea de comandos
	 */
	public static void main(String[] args){
		//Si no se le pasa argumento se finaliza el programa
		if(args.length==0)
			System.out.println("Uso: programa access_log");
		else{
			try{
				//Buffers para gestionar el fichero de entrada
				FileReader fr = new FileReader(args[0]);
				BufferedReader bf = new BufferedReader(fr);
				
				//sCadena almacena una linea del fichero con el siguiente formato
				String sCadena;//="5.39.218.201 - - [19/Feb/2017:14:44:16 +0100] \"GET /82172316237 HTTP/1.1\" 404 209 \"-\" \"Mozilla\"";
				String cadenaAux[];
				String cadenaAux2[];
    		
				String dirIP;
				String fecha;
				String metodo;
				String status;
				String tamano;
				String acceso;
				String agente;
				
				//Obtenemos número de lineas del fichero
				int nEntradas=(int)bf.lines().count();
				
				//Variable que almacena
				String registro[][] = new String[nEntradas][7];

				/*Se resetea la variable que servira de indice para 
				introducir datos en la variable registro*/
				nEntradas=0;
				
				fr = new FileReader(args[0]);
				bf = new BufferedReader(fr);
				while ((sCadena = bf.readLine())!=null) {
					cadenaAux=sCadena.split("\\[");
					//cadenaAux
					//[0]ip - - 
					//||[||
					//[1]fecha] "GET" status size "acceso" "agente"
        		
					//de la primera parte obtengo la ip
					cadenaAux2=cadenaAux[0].split(" ");
					//cadenaAux2
					//[0]ip 
					//|| ||
					//[i] - -
					dirIP=cadenaAux2[0];
        		
					
					cadenaAux2=cadenaAux[1].split("\\]");
					//cadenaAux2
					//[0]fecha
					//||]|| 
					//[1] "GET" status size "acceso" "agente"
					//continuo para obtener la fecha
					fecha=cadenaAux2[0];//formato.parse(cadenaAux2[0]);
        		
					
					//obtengo el metodo
					cadenaAux=cadenaAux2[1].split("\"");
					
					//Las siguientes lineas sirven para corregir los errores de split si hay algun simbolo " en la peticion http
					metodo="";
					if(cadenaAux.length!=6){
						for(int b=1;b<cadenaAux.length-4;b++){
							metodo=metodo+cadenaAux[b];
						}
					}else{
						metodo=cadenaAux[1];
					}
					//cadenaAux
					//[0]
					//[1]GET
					//||]|| 
					//[2] status size 
					//[3]acceso
					//[4] 
					//[5]agente
					//obtengo metodo, acceso y agente
					acceso=cadenaAux[cadenaAux.length-3];
					agente=cadenaAux[cadenaAux.length-1];
        		

					cadenaAux2=cadenaAux[cadenaAux.length-4].split(" ");
					//cadenaAux2
					//[0] 
					//[1]status
					//[2]size 
					status=cadenaAux2[1];
					tamano=cadenaAux2[2];
        		
					registro[nEntradas][0]=dirIP;
					registro[nEntradas][1]=fecha;
					registro[nEntradas][2]=metodo;
					registro[nEntradas][3]=status;
					registro[nEntradas][4]=tamano;
					registro[nEntradas][5]=acceso;
					registro[nEntradas][6]=agente;
					
					nEntradas++;
				}	
        	
				/*Quedan almacenados en registro[][] los valores
				System.out.println("nRegistros: "+nEntradas);
        		for(int a=0;a<nEntradas;a++){
        			System.out.println("ip: "+registro[a][0]);
        			System.out.println("fecha: "+registro[a][1]);
        			System.out.println("metodo: "+registro[a][2]);
        			System.out.println("status: "+registro[a][3]);
        			System.out.println("tamano: "+registro[a][4]);
        			System.out.println("acceso: "+registro[a][5]);
        			System.out.println("agente: "+registro[a][6]);
        			System.out.println("-----------------------");
        		}*/
				
				
				//Lectura de teclado
				InputStreamReader isr = new InputStreamReader(System.in);
				BufferedReader br = new BufferedReader (isr);
        	
				char opcion='m';
				
				//Variables para ordenar
				Enumeration<String> e; 
				Object clave;
				
				//Bucle hasta que la opcion seleccionada sea la q
				while(opcion!='q'){
					System.out.println("Menu de analisis de fichero de acceso de Apache. Seleccione una opcion.");
					System.out.println("a)Numero de conexiones por IP\nb)Numero de visitas por pagina\nc)Informacion de una IP\nd)Generar grafica de tiempo\nq)Salir");
					String entrada=br.readLine();
					opcion=entrada.toLowerCase().charAt(0); //Se coge la primera letra leida introducida por teclado
        		
					switch(opcion){
					case 'a':
						//Calculamos cuantas conexiones hay desde cualquier IP, tomando en cuenta que una nueva conexion solo se cuenta
						//si durante el tiempo indicado en TIEMPO_ENTRE_CONEXIONES no hay registro de esa ip
						Hashtable<String,Integer> resultado = conexionesPorIP(registro);
        			
						//Ordenamos la lista y la imprimimos por pantalla
						ArrayList<Map.Entry<?, Integer>> l=sortValue(resultado);
						for(int b = 0;b<l.size();b++){
							System.out.println(l.get(b));
						}
						System.out.println("\n");
						break;
					case 'b':
						//Calculo cuantas veces se visita cada página
						Hashtable<String,Integer> resultado2 = nVisitasPagina(registro);
        			
						//Ordenamos la lista y la imprimimos por pantalla
						ArrayList<Map.Entry<?, Integer>> ll=sortValue(resultado2);
						for(int b = 0;b<ll.size();b++){
							System.out.println(ll.get(b));
						}
						System.out.println("\n");
						break;
					case 'c':
						//Opcion para imprimir los registros Fecha Pagina_visitada de una IP determinada
						System.out.println("Introduce una direccion IP:");
						String ip=br.readLine();
						System.out.println();
						infoIP(ip,registro);
						System.out.println();
						break;
					case 'd':
						//Opcion para generar un grafico con el numero de accesos segun la hora
						//Se genera en un archivo png
						System.out.println("Introduce la precision de las muestras(en minutos):");
						String muestras=br.readLine();
						System.out.println();
						try{
							Integer precision=Integer.parseInt(muestras);
							generarGrafica(registro,precision);
						}catch(NumberFormatException e1){
							System.out.println("Error al leer el digito\n");
						}
						break;
					case 'q':
						//Salir del programa
						System.out.println("Saliendo....\n");
						break;
					default:
						System.out.println("Opcion no existente\n");
						break;
					}
				}
			}catch (Exception e){
				System.out.println(e);
			}
		}
	}
	
	/**
	 * Función para ordenar los elementos de un Hashtable
	 * @param t Hashtable a ordenar
	 * @return ArrayList con los elementos del Hashtable ordenador
	 */
	public static ArrayList<Map.Entry<?, Integer>> sortValue(Hashtable<?, Integer> t){

		//Transfer as List and sort it
	    ArrayList<Map.Entry<?, Integer>> l = new ArrayList(t.entrySet());
	    Collections.sort(l, new Comparator<Map.Entry<?, Integer>>(){
	    	public int compare(Map.Entry<?, Integer> o1, Map.Entry<?, Integer> o2) {
	    		return o1.getValue().compareTo(o2.getValue());
	    	}});
	    return l;
	}
	
	/**
	 * Devuelve el número de conexiones por cada IP con diferencia de TIEMPO_ENTRE_CONEXIONES segundos entre ellas
	 * @param registros Tabla de n*7 con los datos de las conexiones del fichero access
	 * @return Hashtable con el número de conexiones por cada IP
	 */
	public static Hashtable<String,Integer> conexionesPorIP(String[][] registros){
		Hashtable<String,Integer> resultado = new Hashtable<String,Integer>();
		//Formato de fecha
		SimpleDateFormat formato=new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss zzzzz");
		
		//Obtenemos las direcciones IP distintas que se han conectado
		String[]ip=cadenasDistintas(registros,0);
		Date ultimaHora=null;
		Date horaAux=null;
		
		int i,a;
		//Para cada valor de la variable ip
		for(i=0;i<ip.length;i++){
			for(a=0;a<registros.length;a++){
				//Las conexiones en la variable registros estan ordenadas por tiempo
				if(ip[i].compareTo(registros[a][0])==0){
					//Si es el primer registro que tenemos
					if(ultimaHora==null){
						try {
							//Actualizamos la hora de conexion
							ultimaHora=formato.parse(registros[a][1]);
							resultado.put(ip[i], 1);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else{
						try {
							horaAux=formato.parse(registros[a][1]);
							//Si ha pasado mas tiempo del que hemos configurado entre los registros
							if(horaAux.getTime()-ultimaHora.getTime()>TIEMPO_ENTRE_CONEXIONES*1000){
								//se añade 1 más al array
								resultado.put(ip[i], resultado.get(ip[i])+1);
							}
							//Actualizamos la hora de conexion
							ultimaHora=formato.parse(registros[a][1]);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			ultimaHora=null;
		}
		
		return resultado;
	}
	
	/**
	 * Devuelve cadena de string con todas las cadenas distintas que hay.
	 * @param registros Variable que contiene los registros de conexión del fichero access. El campo [n][0] contiene la dirección IP. El campo [n][2] contiene el recurso al que se accede.
	 * @param campo Discrimina entre dirección IP y recurso accedido.
	 * @return Devuelve las cadenas distintas existentes en registros.
	 */
	public static String[] cadenasDistintas(String[][] registros,int campo){
		String[] ip=new String[registros.length];
		int index=0;
		
		int i;
		for(i=0;i<registros.length;i++){
			if(campo==0){
				if(!stringRepetido(registros[i][campo],ip)){
					ip[index]=registros[i][campo];
					index++;
				}
			}else if(campo==2){
				if(registros[i][campo].compareTo("-")!=0){
					String[] cadena = registros[i][campo].split(" ");
					if(!stringRepetido(cadena[1],ip)){
						ip[index]=cadena[1];
						index++;
					}
				}
			}
		}
		//Devolvemos cadena de tamaño equivalente al numero de cadenas
		String[] result=new String[index];
		for(i=0;i<result.length;i++)
			result[i]=ip[i];
		
		return result;
	}
	
	
	/**
	 * Comprueba si cadena existe en lista
	 * @param cadena 
	 * @param lista Lista de tamaño n
	 * @return True si cadena existe en lista, False en caso contrario.
	 */
	public static boolean stringRepetido(String cadena, String[] lista){
		for(int i=0;i<lista.length&&lista[i]!=null;i++){
			if(cadena.compareTo(lista[i])==0)
				return true;
		}
		return false;
	}
	
	/**
	 * Método que devuelve el número de elementos del mismo valor en el campo [][2] de la variable registros.
	 * @param registros Variable que contiene los registros de conexión del fichero access.
	 * @return Devuelve Hashtable con el número de visitas por recurso.
	 */
	public static Hashtable<String,Integer> nVisitasPagina(String[][] registros){
		Hashtable<String,Integer> resultado = new Hashtable<String,Integer>();
		
		String[] paginas=cadenasDistintas(registros,2);
		int contador=0;
		
		for(int i=0;i<paginas.length;i++){
			for(int a=0;a<registros.length;a++){
				if(registros[a][2].compareTo("-")!=0){
					if(paginas[i].compareTo((registros[a][2].split(" "))[1])==0){
						contador++;
					}
				}
			}
			resultado.put(paginas[i], contador);
			contador=0;
		}
		
		return resultado;
	}

	/**
	 * Función que devuelve los campos 1 y 2 del parámetro registro cuyo campo 0 coincida con el parámetro ip.
	 * @param ip IP de la que queremos la información.
	 * @param registro Variable que contiene los registros de conexión del fichero access.
	 */
	public static void infoIP(String ip,String[][] registro){
		System.out.println("IP "+ip);
		System.out.println("Fecha\t\t\t\tAcceso");
		for(int i=0;i<registro.length;i++){
			if(ip.compareTo(registro[i][0])==0){
				if(registro[i][2].compareTo("-")==0)
					System.out.println(registro[i][1]+"\t"+registro[i][2]);
				else{
					//Si el registro no es "-" tenemos que quedarnos con una parte del campo
					String[] cadena = registro[i][2].split(" ");
					System.out.println(registro[i][1]+"\t"+cadena[1]);
				}
			}
		}
	}

	/**
	 * Función que calcula el número de conexiones durante 24 horas en tramos indicado por precision.
	 * @param registro Variable que contiene los registros de conexión del fichero access.
	 * @param precision Indica el tamaño de las muestras de tiempo. En minutos.
	 */
	public static void generarGrafica(String[][] registro,int precision){
		InputStreamReader isr = new InputStreamReader(System.in);
    	BufferedReader br = new BufferedReader (isr);
    	
		System.out.println("Se va a generar una gráfica en formato png.");
		System.out.println("¿Desea que se muestren por pantalla los datos? (y/n)");
		String entrada;
		try {
			entrada = br.readLine();
		
			char opcion=entrada.toLowerCase().charAt(0);
			//Creamos una tabla de enteros del tamaño necesario para todas las muestras
			int horas[]=new int[(int)24*60/precision];

			for(int i=0;i<registro.length;i++){
				DateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss ZZZZZ");
				try {
					Date date = (Date)formatter.parse(registro[i][1]);
					
					Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
					calendar.setTime(date);   // assigns calendar to given date 
					
					//Calculamos el indice en el que se debe introducir los datos segun la hora
					int index=(int)(calendar.get(Calendar.HOUR_OF_DAY)*60/precision+(int)(calendar.get(Calendar.MINUTE)/precision));
					
					//Corrección para no salirnos del tamaño
					if(index>=horas.length)
						index=horas.length-1;
					horas[index]+=1;
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//Segun la pregunta que se realiza al principio, se imprime o no el resultado
			if(opcion=='y'){
				for(int i=0;i<horas.length;i++){
					System.out.format("Hora: %d:%02d nVisitas: %d\n",(i*precision/60),(int)((((float)i*precision/60)-(i*precision/60))*60),horas[i]);
				}
			}
			System.out.println();
			//Llamamos al metodo que genera la grafica
			grafica(horas, precision);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	/**
	 * Método que genera una gráfica con horas en el eje y y precision en el eje x. Hace uso de la librería JFreeChart
	 * @param horas Valores de las muestras
	 * @param precision Distancia entre muestras
	 */
	public static void grafica(int[] horas,int precision){		
		XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries xy = new XYSeries("NConexiones/hora. Precision: "+precision+"min");
        XYPlot plot;

        for (int i = 0; i < horas.length; i++) {
            xy.add((double)i*precision/60, (double) horas[i]);
        }
        dataset.addSeries(xy);

        JFreeChart chart = ChartFactory.createXYLineChart(
                null, null, null, dataset, PlotOrientation.VERTICAL, true, true, false);

        try {
        	String titulo="conexion"+precision+"min.png";
			ChartUtilities.saveChartAsPNG(new File(titulo), chart, 800, 500);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
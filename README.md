#
### Transferencia de archivos por medio de Java RMI (Remote Method Invocation)

Sistema para compartir archivos creado en Java utilizando RMI.
El siguiente sistema contiene dos tipos de entidades: un servidor y un cliente.
El cliente es capaz de subir y descargar archivos del servidor, así como también
puede crear, eliminar y explorar directorios dentro del directorio de almacenamiento
del servidor (ServerStorage).

Para poder hacer usar este código es necesario ejecutar en la línea de comandos o terminal lo siguiente:
1. Compilar todos los archivos de la carpeta "src" y guardar los binarios en la carpeta "bin":
`javac -d ../bin *.java`

2. Para ejecutar el servidor desde la carpeta "bin", se debe de pasar un número puerto disponible en la máquina local:
`java Server start <portnumber>`
*Nota: Al correr el servidor, se crea automáticamente un directorio llamado "ServerStorage" que actúa como directorio raíz de almacenamiento del servidor. El cliente puede subir o bajar archivos, así como crear o eliminar nuevos directorios dentro del directorio ServerStorage. Si no se le pasa un número de puerto al servidor, se usará por defecto el puerto 1099*

3. Configura la ubicación del servidor (IP del servidor y número de puerto) a las variables de entorno:
`set SERVER_PORT=<IP>:<portnumber>`
`export SERVER_PORT=<IP>:<portnumber>`
	- Para eliminar la variable de entorno se debe ejecutar:
`unset SERVER_PORT`

4. Ejecutar el cliente desde la carpeta "bin":
  - Para subir un archivo
`java Client upload <path_on_client> <../ServerStorage/filename>`

  - Para descargar un archivo (FileNotFoundException es arrojada si el archivo no existe).
`java Client download <../ServerStorage/existing_filename_on_server> <path_on_client/newfilename>`

  - Para enlistar los directorios y archivos presentes en ServerStorage:
`java Client ls <../ServerStorage/path_existing_directory_on_server>`

  - Para crear un nuevo directorio:
`java Client mkdir <../ServerStorage/new_directory_name>`

	- Para ver la ruta del directorio raíz (ServerStorage):
`java Client dir`

  - Para eliminar un directorio existente:
`java Client rmdir <../ServerStorage/existing_directory_name>`

  - Otra forma de eliminar un archivo:
`java Client rm <../ServerStorage/existing_file_name>`

  - Para apagar el cliente
`java Client shutdown`

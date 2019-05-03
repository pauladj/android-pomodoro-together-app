<?php


define("DB_SERVER", "localhost"); #la dirección del servidor
define("DB_USER", "Xebracamonte001"); #el usuario para esa base de datos
define("DB_PASS", "wzG6bRdRg"); #la clave para ese usuario
define("DB_DATABASE", "Xebracamonte001_pomodoro"); #la base de datos a la que hay que conectarse


function connect(){
  # Se establece la conexión:
  $con = mysqli_connect(DB_SERVER, DB_USER, DB_PASS, DB_DATABASE);

  #Comprobamos conexión
  if (mysqli_connect_errno($con)) {
    //echo 'Error de conexion: ' . mysqli_connect_error();
    throw new Exception('external_error');
  }

  return $con;
}


function execute($con, $sentence){
  # Ejecutar la sentencia SQL
  $resultado = mysqli_query($con, $sentence);
  # Comprobar si se ha ejecutado correctamente
  if (!$resultado) {
    //echo 'Ha ocurrido algún error: ' . mysqli_error($con);
    //exit;
    throw new Exception('external_error');
  }
  return $resultado;

}

function select_is_empty($resultado){
  if (mysqli_num_rows($resultado) > 0) {
    return false;
  }else{
    return true;
  }
}

function close($con){
  try {
    if (isset($con)){
      mysqli_close($con);
    }
  } catch (Exception $e) {
    //
  }
}

?>

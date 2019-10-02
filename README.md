# AgendaSimple
Aplicación de una Agenda de contactos para la asignatura de Android en el Máster de Desarrollo de Aplicaciones Móviles de la UPM disponible en Español (ES) e Inglés (EN). 

API Mínima = 21.

Esta aplicación es capaz de añadir contactos, eliminarlos y modificarlos cuando se quiera:

  - Inserción: Habilitado un FAB que guía al usuario a una nueva actividad para crear el contacto (ContactOverview).
  - Borrado: En la vista completa de todos los contactos, se debe hacer SWIPE hacia derecha o izquierda para eliminar el contacto.
  - Modificación: Con darle al contacto que se quiera modificar en la lista de contactos, lleva al usuario a ContactOverview pero con los campos rellenados del contacto cuando se insertó.

Se ha utilizado SQLiteOpenHelper para administrar los datos con las operaciones descritas, además de para implementar la búsqueda de contactos.

Además, la aplcación permite llamar al contacto y exportar desde o a la tarjeta SD del dispositivo.

Para mejorar la UI, se ha decidido que, al crear un usuario, se guarde un color aleatorio entre 7 propuestos. Este color se pintará en la lista en cada contacto junto con un texto que representa la inicial del nombre del contacto. Esto se ha denominado Bubble.

También, al modificar un contacto, el ActionBar y la StatusBar (API > 21), se cambian al color del Bubble del contacto.

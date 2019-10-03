# AgendaSimple
Aplicación de una Agenda de contactos para la asignatura de Android en el Máster de Desarrollo de Aplicaciones Móviles de la UPM disponible en Español (ES) e Inglés (EN). 

Esta aplicación es capaz de añadir contactos, eliminarlos y modificarlos cuando se quiera:

  - Inserción: Habilitado un FAB que guía al usuario a una nueva actividad para crear el contacto (ContactOverview).
  - Borrado: En la vista completa de todos los contactos, se debe hacer SWIPE hacia derecha o izquierda para eliminar el contacto. Implementado con el ItemTouchHelper.Callback.
  - Modificación: Con darle al contacto que se quiera modificar en la lista de contactos, lleva al usuario a ContactOverview pero con los campos rellenados del contacto cuando se insertó.

Se ha utilizado SQLiteOpenHelper para administrar los datos con las operaciones descritas, además de para implementar la búsqueda de contactos. Para la visualización de los contactos se ha utilizado RecyclerView con un Adapter básico.

Además, la aplicación permite:

  - Llamar al contacto y exportar desde o a la tarjeta SD del dispositivo.
  - Realizar llamadas de los números del contacto mediante el intent ACTION_DIAL.
  - Realizar búsquedas de contactos mediante SearchView con acceso a la BBDD.
  - Acceso a WhatsApp para mandar un mensaje directo mediante ACTION_VIEW y la API de WhatsApp a un determinado contacto (sólo válido para números con prefijo +34).

Para mejorar la UI, se ha decidido que, al crear un usuario, se guarde un color aleatorio entre 7 propuestos. Este color se pintará en la lista en cada contacto junto con un texto que representa la inicial del nombre del contacto. Esto se ha denominado Bubble.

También y únicamente para dispositivos que tengan nivel de API mayor a 21, al modificar un contacto, el ActionBar, la StatusBar y todos los iconos de ContactOverview, se cambian al color del Bubble del contacto. Si se está por debajo de ese nivel de API, todos esos elementos se mantienen con el color predeterminado de la aplicación. 

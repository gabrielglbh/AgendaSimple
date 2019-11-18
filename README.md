# AgendaSimple
Aplicación de una Agenda de Contactos para la asignatura de Android en el Máster de Desarrollo de Aplicaciones Móviles de la UPM disponible en Español (ES) e Inglés (EN). 

Esta aplicación es capaz de añadir contactos, eliminarlos y modificarlos cuando se quiera:

  - Inserción: Habilitado un FAB que guía al usuario a una nueva actividad para crear el contacto (ContactOverview).
  - Borrado: En la vista completa de todos los contactos, se debe hacer SWIPE hacia derecha o izquierda para eliminar el contacto. Implementado con el ItemTouchHelper.Callback.
  - Modificación: Con darle al contacto que se quiera modificar en la lista de contactos, lleva al usuario a ContactOverview pero con los campos rellenados del contacto cuando se insertó.

Se ha utilizado SQLiteOpenHelper para administrar los datos con las operaciones descritas, además de para implementar la búsqueda de contactos. Para la visualización de los contactos se ha utilizado RecyclerView con un Adapter básico. Como mejora adicional a la base de datos SQLite, se ha creado un ContentProvider para que las demás aplicaciones del dispositivo puedan utilizar los datos de esta aplicación.

Además, la aplicación permite:

  - Exportar todos los contactos y su información al almacenamiento externo del dispositivo mediane un JSON a un archivo CNT.
  - Importar todos los contactos desde la copia de seguridad en el almacenamiento externo.
  - Importar todos los contactos de la lista de contactos propia del móvil con el ContentProvider de ContactsContract.
  - Realizar llamadas de los números del contacto mediante el intent ACTION_DIAL.
  - Realizar búsquedas de contactos mediante un SearchView con acceso a la BBDD.
  - Marcar como favoritos los contactos que se quieran. Aparecerán al principio de la lista.
  - Acceso a WhatsApp para mandar un mensaje directo mediante ACTION_VIEW y la API de WhatsApp a un determinado contacto (sólo válido para números con prefijo +34).
  - La utilización de la base de datos propia en aplicaciones de terceros mediante la creación de un ContentProvider propio.
  - Acceso al Correo Electrónico para mandar directamente un correo a un contacto si éste tiene un correo asociado.
  - Opción de añadir una foto de contacto de la galería y otras aplicaciones mediante el intent ACTION_PICK. Se hace uso de la librería Picasso para cargar las imágenes en el RecyclerView.
  - Opción de añadir, modificar y cancelar citas con contactos con DatePickerView y TimePickerView con exportación a Google Calendar, con opción a añadir recordatorios.

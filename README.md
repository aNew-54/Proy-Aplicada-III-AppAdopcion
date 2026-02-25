# 🐾 App de Adopción de Mascotas

Una aplicación móvil diseñada para conectar refugios de animales con posibles adoptantes, facilitando el proceso de encontrar un nuevo hogar para mascotas rescatadas. La plataforma permite explorar mascotas disponibles, gestionar perfiles de refugios y administrar listas de favoritos.



## 🛠️ Tecnologías y Arquitectura

Este proyecto está construido bajo los estándares modernos de desarrollo móvil, asegurando un código escalable y mantenible:

* **Plataforma:** Android (Nativo)
* **Lenguaje:** Java
* **Backend / Base de Datos:** Supabase (PostgreSQL)
* **Arquitectura:** MVVM (Model-View-ViewModel) con patrón Repository.
* **Diseño:** Material Design 3



---

## 🚀 Configuración del Proyecto (Setup)

1. Clonar el repositorio.
2. Abrir el proyecto en Android Studio.
3. Crear un archivo `local.properties` en la raíz del proyecto (si no existe).
4. Agregar las credenciales del entorno (solicitar al administrador del repositorio):
   ```properties
   SUPABASE_URL=tu_url_aqui
   SUPABASE_KEY=tu_key_aqui

5. Sincronizar Gradle y ejecutar en el emulador o dispositivo físico.



# 📋 Repartición de Tareas - Equipo de Desarrollo (App Adopción)

Este documento detalla las responsabilidades y los requerimientos específicos de interfaz (UI) y lógica de negocio asignados a cada miembro del equipo. 

> **Nota:** Las funcionalidades de "Solicitudes de adopción" se encuentran fuera del alcance (Out of Scope) para esta fase del desarrollo y han sido omitidas.

---

## 👩‍💻 1. Anthonela
**Módulo:** Fragmento "Descubrir" (Feed General de Mascotas - RF4)

**Responsabilidades:**
* **Interfaz de la pestaña "Descubrir":** Crear la ventana principal donde se listan todas las publicaciones de todos los refugios.
* **Barra de Búsqueda:** Implementar búsqueda por nombre, raza o especie.
* **Filtros y Ordenamiento (Menú contraíble):**
    * Botón para cambiar el orden de la lista (Ascendente / Descendente).
    * Botón para tipo de orden (Por cantidad de favoritos o por fecha de publicación).
    * Implementar chips/etiquetas dinámicas de las razas obtenidas de la base de datos.
* **Interacciones del Feed:**
    * Redirigir a los "Detalles de la Mascota" al tocar la imagen o descripción de una tarjeta.
    * Asegurar que el botón de "Agregar a Favoritos" en la tarjeta solo sea funcional/visible si el usuario actual es un **Adoptante**.
    * Si se toca la foto de perfil del refugio se redirige al perfil del refugio (Vista hecha por Arnold)
* **Colaboración:** 
    * Coordinar con *Alexander* para que el *Floating Action Button* (FAB) de "Agregar mascota" viva en esta pantalla (solo visible para Refugios).
    * Crear la vista card de las publicaciones para que pueda ser utilizado por *Arnold*
---

## 👨‍💻 2. Brayam
**Módulo:** Detalles de Mascota (RF5) y Edición de Perfil

**Responsabilidades:**
* **Vista de Detalles de Mascota:**
    * Diseñar la UI completa para mostrar toda la información de la mascota seleccionada.
    * **Lógica de Favoritos:** Implementar el botón de corazón. Si el usuario es un Adoptante, el clic lo añade a su lista. Si es un Refugio, el botón debe estar oculto.
    * **Ubicación:** Chip de ubicación que, al recibir clic, abra el mapa (idealmente mostrando la ruta entre el adoptante y la mascota).
    * **Contacto:** Botón de enviar mensaje que abra WhatsApp con el número del refugio asociado y un texto predefinido.
* **Vista de Editar Perfil (8va Vista):**
    * Crear la pantalla de configuración/edición de datos del usuario.
    * Debe ser accesible tanto para el Adoptante como para el Refugio desde su respectiva pestaña "Yo".

---

## 👨‍💻 3. Arnold
**Módulo:** Directorio de Refugios y Perfil del Refugio (RF8)

**Responsabilidades:**
* **Fragmento "Refugios" (Pestaña inferior):**
    * Barra de búsqueda superior (por nombre de refugio).
    * **Segmented Button:** Control para alternar entre "Vista de Lista" y "Vista de Mapa".
* **Vista de Lista:** Tarjetas de refugios que al tocarlas envían a la vista detalle.
* **Vista de Mapa:** * Pintar los refugios en el mapa.
    * Al tocar un pin, mostrar ventana emergente (burbuja de diálogo) con info resumida. 
    * Al tocar la burbuja, enviar a la vista detalle.
* **Vista Detalle de Refugio:**
    * Diseñar el perfil público del refugio.
    * Chip de ubicación que abra el mapa enfocado en el refugio.
    * Mostrar la lista de publicaciones (mascotas) de ese refugio. (Al tocar una tarjeta, enviar al Detalle de Mascota).
    * Si el usuario que ingresa a esta seccion es un refugio, no puede añadir a favoritos a las mascotas
    * Puede usar las cards que se cree en el feed para mostrar las mascotas (Similar al prototipo)

---

## 👨‍💻 4. Alexander
**Módulo:** Creación de Publicaciones e Historial Médico (RF2)

**Responsabilidades:**
* **Botón de Creación:** Implementar el *Floating Action Button* (FAB) inferior en el fragmento "Descubrir" (Lógica de visibilidad: Solo para Refugios).
* **Formulario "Agregar Nueva Mascota":** * Crear la vista y validaciones para registrar una mascota en la base de datos.
* **Información Médica (5to Fragmento):**
    * Diseñar e integrar la sub-vista o sección dentro del formulario para registrar información sobre **Vacunas Básicas**.
    * Sección para agregar el historial de **Intervenciones Médicas**.

---

## 👨‍💻 5. Darick
**Módulo:** Lista de Favoritos (Adoptante - RF6)

**Responsabilidades:**
* **Vista de Mascotas Favoritas:** * Diseñar la lista de tarjetas para la pestaña "Yo" del Adoptante.
    * Mostrar las mascotas guardadas por el usuario.
    * Al tocar una tarjeta, debe redirigir al "Detalle de mascota" (Vista de Brayam).
* **Lógica del Perfil (Adoptante):**
    * Conectar el botón "Ver favoritos" en la pantalla del perfil del Adoptante.
    * Mostrar en el perfil del refugio el dato: "Mascota con más veces puesto en favoritos".

---

## 👩‍💻 6. Sarah
**Módulo:** Lista de Mascotas Publicadas (Refugio)

**Responsabilidades:**
* **Vista de Mascotas del Refugio:** * Diseñar el listado que aparecerá en la pestaña "Yo" cuando el usuario sea un Refugio.
    * Crear tarjetas simples con detalles resumidos de la mascota publicada.
    * Al darle clic a la tarjeta, redirigir a los "Detalles de mascota" (Vista de Brayam).
* **Estructura Base del Perfil "Yo" (Refugio):**
    * Asegurar que el perfil muestre correctamente la Foto de perfil, Datos del usuario y el botón de Cerrar Sesión al final de la vista.

---

## 🏗️ Notas Generales de Arquitectura para el Equipo
* **Fragmento "Yo" (Usuario):** Si se puede, conectar los botones de "yo" como *Ver mis favoritos* de adoptante y *Ver mis mascotas* con las vistas correspondientes.
* **Navegación:** Todos los miembros que listen mascotas (Anthonela, Arnold, Darick, Sarah) deben asegurarse de redirigir correctamente al Fragmento "Detalle de Mascotas" creado por Brayam pasando el ID correspondiente de la mascota.
* **Arquitectura MVVM:** Separar las responsabilidades entre la vista y la logica usando los view models
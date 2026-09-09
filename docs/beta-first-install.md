# Primera Instalacion De La Beta

Actualizado 2026-09-09. Alejandro ya tiene cuenta de Play Console. Todavia no se ha verificado una carga firmada de esta version ni una instalacion fisica. La consulta de nombres de secretos en GitHub no devolvio secretos de repositorio; no se inspeccionaron valores.

## Lo Que Necesitamos Del Fundador

1. Confirmar que la ficha de Play se llama Jikanle y usa `co.com.jikanle`. Indicar si la cuenta es personal u organizacion; no enviar credenciales.
2. Configurar la clave de carga. Si no existe, Android Studio > Build > Generate Signed App Bundle / APK > Android App Bundle > Create new. Guardar el keystore fuera del repo, conservar copia privada y contrasenas. Si ya existe una clave para esta app, reutilizarla.
3. En GitHub `Jikanle/android-app` > Settings > Secrets and variables > Actions, guardar `SIGNING_KEYSTORE_BASE64`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`, `SIGNING_STORE_PASSWORD`, `SUPABASE_URL`, `SUPABASE_ANON_KEY`. Base64 es codificacion, no cifrado: solo pegarlo en GitHub Secrets, nunca en el chat. `GOOGLE_OAUTH_CLIENT_ID` queda disponible para configuracion Google; habilitar el proveedor en Supabase por separado.
4. Con el responsable del backend, aplicar/verificar `supabase/beta_events.sql` y `organization_communities.sql` despues del esquema/calendario existente. Seguir las pruebas RLS en `beta-contract.md`. No ejecutar todo `schema.sql` a ciegas sobre produccion.
5. En Supabase Auth permitir `jikanle://auth-callback`; configurar Email/Google y probar confirmacion de email. Configurar Google mediante el callback del proyecto que muestra Supabase. No desactivar verificacion de correo para esconder fallos.

## Crear Y Subir El Archivo

Con el commit de beta publicado y CI aprobado: GitHub Actions > Android Release > Run workflow. Elegir la rama que contiene este cambio, `version_name=0.2.0` y un `version_code` superior a cualquier version cargada a Play. Mantener `publish_release=false` para la primera prueba. Esta version del workflow valida firma y Supabase, ejecuta pruebas/lint y produce AAB + APK. Los secretos se inyectan por entorno, sin editar `local.properties`.

Descargar y descomprimir el artefacto `jikanle-release-aab`. En Play Console > Jikanle > Test and release > Testing > Internal testing > Create new release: activar Play App Signing, cargar el archivo `.aab` (no el ZIP ni el APK), introducir notas, guardar, revisar y publicar al canal interno. Resolver cualquier requisito que muestre tu consola.

## Anadir Tu Correo E Instalar

En Internal testing > Testers, crear una lista privada, anadir `alesanchezpov@gmail.com` y guardar. Copiar el enlace de participacion. Abrirlo en el telefono con esa misma cuenta de Google, aceptar participar y abrir el enlace de Play Store para instalar. Ser administrador/desarrollador no te inscribe automaticamente como tester. La cuenta de Google del telefono y la lista deben coincidir. No hace falta reclutar 12 personas para tu primera instalacion interna.

Si el telefono tiene un debug APK o un APK firmado con otra clave, puede no aceptar la actualizacion. La desinstalacion borra el progreso local: comprobarlo antes de cambiar de firma. El AAB no se instala directamente con adb.

Para probar antes de Play: conectar el telefono por USB, activar depuracion USB y aceptar la autorizacion. Desde el repo:

```bash
export JAVA_HOME="$HOME/.local/share/JetBrains/Toolbox/apps/android-studio/jbr"
./gradlew :app:installDebug
```

## Recorrido De Aceptacion

1. Comunidad: abrir agenda de Luma; si hay eventos publicados, comprobar reserva, fecha y calendario. La reserva se completa en Luma, no en Android.
2. Leccion: avanzar hasta vocabulario; cerrar/reabrir y comprobar posicion. Repetir sin red, en ES/EN y modo claro/oscuro. No debe haber texto tapado a 200% de fuente.
3. Escucha: abrir busqueda de Spotify y volver. No esperar reproduccion interna ni evaluacion de pronunciacion.
4. Terminar: confirmar el ultimo paso, volver a Comunidad y reabrir la leccion terminada.
5. Cuenta: iniciar sesion, habilitar datos de uso si deseas participar y dar una valoracion. Con red/SQL correcto debe indicar enviada; sin red debe indicar pendiente y permitir reintentar.
6. Backend: comprobar una sola fila por UUID, aislamiento entre dos cuentas y ausencia de correos/letras/audio en la tabla. Usar `docs/beta-metrics.sql` para conteos de participantes autenticados que aceptaron compartir.

## Siguiente Cohorte

Primero equipo interno, luego participantes de eventos. Las cuentas personales creadas despues del 13-11-2023 necesitan 12 testers inscritos continuamente durante 14 dias en una prueba cerrada para solicitar produccion. No es un requisito para el canal interno. Revisar las instrucciones actuales de [Google sobre pruebas](https://support.google.com/googleplay/android-developer/answer/14151465?hl=es) y [configuracion de canales](https://support.google.com/googleplay/android-developer/answer/9845334?hl=es). El tiempo interno no sustituye el cerrado. No prometer aprobacion automatica ni fecha de revision fija.

Pendientes antes de una beta mas amplia: politica de privacidad publicada y coherente, retencion/eliminacion de datos y cuenta, ficha exacta, pre-launch report, evidencia de auth y dispositivos. La app no incluye un SDK de crash reporting; usar Play Console y logs redactados para diagnostico. No afirmar crash-free sessions a partir de la tabla de uso.

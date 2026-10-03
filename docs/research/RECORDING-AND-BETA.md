# Primera toma JA -> PT y beta Android

2026-10-03. Plan de grabacion/aceptacion; no audio sintetizado ni publicacion en Play
ejecutados en esta sesion. La app no sustituye una estacion de audio.

## Cuatro departamentos, una revision conjunta

| Departamento | Responsables | Entrega |
|---|---|---|
| Tecnologia / IA | Alejandro + Robert | Artefactos versionados, adaptadores, mediciones, auditoria y reproducibilidad |
| Musica / audio, independiente | Andres + Santiago | Frases/notas/acentos, respiraciones, interpretacion, mezcla y escucha critica |
| Letra / traduccion | Alejandro; Rafa revisa JA; Alejandro revisa PT | Significado, ambiguedad, glosa, alternativas cantables y cambios |
| Pedagogia / idiomas | Alejandro + David Daza | Objetivo para lusofonos que estudian JA; actividad y evaluacion humana |

Rafa aporta tambien HCI/computacion visual; Alejandro coordina eventos. Robert audita
inferencias pero no sustituye a los musicos. Registrar revisiones por version de
candidato; cambiar letra/toma invalida aprobaciones relevantes. Un buen sonido no
compensa significado falso. El equipo de musica conserva criterio y backlog propios.

## Mapeo de frase

Usar `phrase-review-template.csv` en carpeta privada. Una fila por segmento con ID
estable, posicion real en audio y unidad de significado. Primero glosa literal PT;
luego dos candidatos cantables. Alinear moras/fonemas JA con notas, y silabas/fonemas
PT con posiciones musicales. No igualar cantidades a ciegas ni traducir palabra por
nota. Cada cambio artistico requiere una razon y revision bilingue/musical.

Separar cuatro restricciones:

1. Semantica: agente, destinatario, tiempo, imagen, emocion interpretada; anotar ambiguedad.
2. Prosodia: acento lexico PT, vocales, consonantes, duraciones y frases.
3. Musica: contorno, notas, beat/subdivision, tension y respiracion. Acento musical no
   equivale siempre a primer tiempo; emocion tampoco equivale a tonalidad.
4. Produccion: cantante, tesitura, timbre consentido, dinamica, colocacion y mezcla.

"Preservar tono" puede significar afecto, melodia, tonalidad o timbre: son cosas
distintas. Propongo preservar significado/contorno con transposicion permitida para
tu voz; confirmar con los musicos. No imitar una identidad vocal sin permiso.
La romanizacion aportada no es una transcripcion japonesa validada. Revisar el
contexto y escuchar la adaptacion cantada antes de aprobarla.

## Primera grabacion para DaVinci

1. Elegir el fragmento antes de arreglar toda la cancion. Baseline: toma propia o
   cantante consentido. Voz sintetica requiere un modelo apto para canto PT, no
   solo TTS; aun no hay un modelo JA/PT validado en este proyecto.
2. Preparar base autorizada y guia. Separar stems de un master no concede una licencia.
   No subirlo a proveedores antes de acordar permisos/condiciones. Conservar original.
3. Grabar voz seca con auriculares, sin base por altavoces, ganancia sin clipping y
   prueba corta de ruido/diccion. Guardar violin y voz separados y tomas sin efectos
   destructivos. No hace falta comprar equipo antes de conocer el que ya tienes.
4. Propuesta de intercambio: WAV PCM 48 kHz/24 bit, mismo cero e igual duracion para
   los stems. Convertir una copia si la fuente es 44.1 kHz; no mejora la informacion
   original. Decidir frame rate del video antes de editar.
5. Entregar cuando existan: `guide.wav`, `backing.wav`, `voice_pt_dry_take01.wav`,
   `violin_take01.wav`, `rough_mix.wav`, con revision/hash/origen temporal.
6. En Resolve: importar video y stems, fijar ancla comun, editar/mezclar en Fairlight.
   Marcar inicio de frase, palabra enfatizada y gesto. Montar desde tiempos medidos,
   no un timestamp supuesto de YouTube. Subtitulos siguen la toma realmente cantada.
7. Revisar auriculares y telefono con nivel comparable entre tomas. Corregir diccion,
   texto y respiracion antes de mas procesamiento. Aprobar JA/PT y musica antes de
   exportar para publicar; no llamar master a una premezcla sin revision.

Material oficial: [Fairlight/Resolve](https://www.blackmagicdesign.com/products/davinciresolve/training).
Confirmar sistema operativo, version y micro/interfaz para fijar formatos compatibles.
No se instalo un DAW/modelo pesado ni se reservo computo universitario.

## Probar la app hoy

Debug: Leccion -> Songbridge -> importar estudio JSON privado. Debe mostrar original,
traduccion, vocabulario y notas en orden; NO canto generado o sincronizacion automatica.
El importador privado esta deshabilitado en release. La beta Play usa contenido
distribuible, no el archivo privado de Fukahi.

Beta/Cuenta -> Preferencias de eventos: guardar/reabrir/retirar, comprobar etiquetas
y exclusion de metricas en servidor. Probar red ausente, reintento, dos cuentas,
ES/EN, claro/oscuro y texto grande. Sin SQL desplegado mostrara error, no exito local.

Siguiente incremento musical: audio autorizado + Media3, seleccion de dos tomas,
tiempos reales por frase y comparacion original/adaptacion. Backend guarda analisis
y revisiones separados; Android presenta resultados. No prometer cover automatico
antes de validar el canto PT y sus permisos.

## Instalar desde Play Store

1. Confirmar ficha `co.com.jikanle`, tipo de cuenta, correo Google tester y mayor
   versionCode usado. Configurar firma/Supabase en GitHub Secrets, no en el chat.
2. CI verde de rama aprobada -> Android Release con versionCode superior y
   `publish_release=false`. Descargar AAB firmado; no instalar AAB con adb.
3. Play Console -> Prueba interna -> cargar AAB -> revisar/publicar. Agregar tu
   correo en Testers, guardar y aceptar el enlace con la misma cuenta del telefono.
4. Instalar desde ese enlace de Play Store. No equivale a ficha publica buscable.
   Poner en el sitio "Participar en la beta" solo cuando el enlace real funcione;
   no inventar URL ni afirmar disponibilidad general.

El canal interno admite hasta 100 testers; el enlace inicial puede tardar en propagarse.
[Guia oficial](https://support.google.com/googleplay/android-developer/answer/9845334?hl=es).
12 testers/14 dias corresponde al acceso a produccion de determinadas cuentas
personales nuevas, no a tu primera instalacion interna.
[Requisitos](https://support.google.com/googleplay/android-developer/answer/14151465?hl=es).
Firma y recorrido: `../beta-first-install.md`.

Pendientes: build verde, dispositivo fisico, contrato combinado Web/backend, auth
real, privacidad/eliminacion y observacion consentida. No hay enlace Play confirmado,
release subida ni archivo musical listo para Resolve en esta entrega.

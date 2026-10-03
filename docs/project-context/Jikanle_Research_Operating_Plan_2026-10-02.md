# Jikanle: identidad, investigación y prototipo JA-PT

Fecha: 2026-10-02. Estado: plan de trabajo y decisiones propuestas; no certifica un
backend desplegado, una licencia musical ni resultados educativos. Audiencia: equipo
fundador de seis personas y colaboradores de música, lenguas y pedagogía.

Actualización 2026-10-03: el piloto enseña japonés a lusófonos; comprensión y
vocabulario primero, producción libre después. Los cuatro departamentos conjuntos
son Tecnología/IA, Música/audio, Letra/traducción y Pedagogía/idiomas. Entregas y
responsables: `docs/research/RECORDING-AND-BETA.md` en Android. El laboratorio MIR
incluye ocho papers y evaluación reproducible de tiempos, no inferencia musical
implementada. Preferencias/retirada e importación de asistencia pasan pruebas locales;
no hay despliegue Supabase ni carga Play confirmada. Las fechas urgentes anteriores
son históricas, no una promesa de publicación cumplida.

## 1. Identidad y prioridad

**Decisión del fundador, confirmada durante esta sesión:** el resultado integra
comparación de letra, cover portugués evaluado y video temático con letra/música.
Jikanle busca un adaptador de covers, no traducción textual aislada. La fecha sigue
pendiente. El prototipo textual actual es un paso de inspección, no el producto final.

Audio previsto por Alejandro: máster original combinado con interpretación propia de
violín y, posteriormente, otros músicos. Primera demostración digital urgente; versión
más producida aproximadamente dos semanas después. Fecha exacta pendiente por la
contradicción entre «mañana 1 de octubre» y la fecha del entorno, 2 de octubre.
Alejandro plantea fair use por transformación; esa base no verifica por sí sola permisos
de máster/adaptación/video/entrenamiento. Mantener la revisión de derechos pendiente.

Jikanle conecta aprendizaje de idiomas, música y pertenencia presencial. El producto
mínimo conserva dos recorridos: descubrir/volver a un encuentro y continuar con una
experiencia musical-lingüística. La investigación permite mejorar esa experiencia;
no debe reemplazarla por una colección de experimentos inconexos.

Propuesta a confirmar: Jikanle es la marca del producto/comunidad; Acervo Labs es el
programa de investigación, no otra identidad comercial que debamos lanzar ahora.
La ventaja buscada es contenido autorizado, evaluación reproducible y selección
pedagógica específica para una persona/objetivo, no un número universal de calidad.
No anunciar aún «el mejor modelo musical» ni aprendizaje clínicamente demostrado.

El piloto Brasil-Japón usa Fukahi (不可避), interpretada por 島爺, identificada en el
[sitio oficial del anime](https://ragnarok-official.com/1st/news/94/). El enlace prueba
la identidad de la obra, no autoriza descargar, adaptar, entrenar o publicar su audio.

## 2. Estado real y pendientes

| Área | Disponible | Falta para una prueba completa |
|---|---|---|
| Android | Compose, lección, caché, demo de comparación, portugués y apertura local de JSON en debug | Prueba física, revisión visual y lectura remota del estudio aprobado |
| Texto | Validación de orden/cobertura, borrador JA-PT privado, letra romanizada aportada por Alejandro | Transcripción japonesa revisada; traducción literal y cantable diferenciadas; revisor JA/PT |
| Audio | FFmpeg/ffprobe instalados; inspector local de duración/formato/hash/intervalo | Archivo autorizado, tiempos reales, anotaciones y toma portuguesa |
| MIR | Frontera de datos y protocolo | Alineador, segmentación musical y métricas contrastadas con anotación humana |
| Backend | Supabase compartido y función textual existente con soporte PT preparado | Despliegue revisado, permisos, cuotas, atomicidad, estado de trabajos y almacenamiento privado |
| Eventos | Agenda/RSVP externo en Android; normalizador offline, cadencias y pruebas de métricas | Fuentes autorizadas, formulario/consentimiento, check-in, cola persistente y transporte |
| Investigación | Responsabilidades y contratos propuestos | Responsables confirmados, objetivo educativo y protocolo de evaluación |
| Distribución | Herramientas de build y release existentes | Firma, secretos operativos, QA físico y carga real a Play |

El compositor de canciones, análisis de pronunciación cantada, separación vocal,
entrenamiento propio y sincronización automática **no están implementados** por este
trabajo. La demo local no equivale a un servicio backend operativo.

## 3. Cómo introducir canción, letra y audio

Una carpeta privada por estudio, fuera de Git y fuera de una carpeta pública de Drive:

```text
research-local/fukahi/
  intake.json              identidad, procedencia, permisos por finalidad
  original.wav             archivo permitido, inmutable
  lyrics.romaji.txt         transcripción del fundador, todavía sin verificar
  lyrics.ja.txt             transcripción revisada por especialista
  annotations/             líneas, frases, beats, notas y discrepancias
  takes/                   interpretación portuguesa autorizada
  outputs/                 manifiestos y borradores versionados
```

Ya se guardó el texto proporcionado en `research-local/fukahi-lyrics.romaji.txt` y un
estudio del fragmento en `research-local/fukahi-study.json`. Ambos son privados e
ignorados por Git. No se suben con este documento. No hace falta introducirlos otra vez.
El portugués actual es una glosa provisional, no una traducción profesional aprobada.

No derivar kanji, límites morfológicos ni pitch accent «correctos» a partir de romaji
sin revisión: la romanización puede ocultar homófonos y decisiones de segmentación.
Conservar la versión original aportada; toda corrección es una versión nueva con autor.

Para inspeccionar un archivo local permitido desde el repositorio Android:

```bash
python3 tools/research/inspect_audio.py research-local/original.wav \
  research-local/audio-manifest.json --start-ms 10000 --end-ms 20000
```

Los tiempos son **solo un ejemplo de comando**, no el intervalo de Fukahi. Deben
sustituirse por los tiempos medidos en tu archivo. El inspector obtiene duración,
canales, muestreo, códec, hash y referencia temporal. No escucha ni evalúa la canción.
No sobrescribe un manifiesto previo. Audio, transcripción y take deben compartir una
referencia temporal explícita: inicio del archivo o inicio del extracto, nunca mezclada.

```bash
deno run --allow-read=research-local --allow-write=research-local \
  tools/research/run-study.ts research-local/fukahi-study.json \
  research-local/fukahi-android.json
```

El segundo comando exporta texto para Android debug. No envía datos a proveedores,
no procesa el audio y no pide claves. En Android: Lección -> Japonés y portugués ->
Abrir estudio local. Para reproducir audio integrado todavía hay que implementar
una fuente autorizada y el adaptador de reproducción, sin descargar de Spotify/YouTube.

## 4. Entregables por departamento

| Área / responsable propuesto | Entrada | Salida | Revisión cruzada |
|---|---|---|---|
| Japonés / Alejandro + Rafa; portugués / Alejandro | Texto contextualizado y objetivo | Significado, pronombres/tiempo, registro, imágenes, ambigüedad, glosa y candidatos cantables | Andres/Santiago revisan prosodia; validación PT externa deseable antes de claims públicos |
| Música de entrada / Andres | Audio permitido y texto revisado | Frases, tensión/resolución, beats, notas, respiraciones, segmentación y confianza | Segundo anotador verifica tiempos |
| Música de salida / Santiago | Candidato portugués y guía musical | Toma vocal, interpretación, cambios deliberados, balance/espacio y evaluación auditiva | Revisor PT comprueba naturalidad y comprensión |
| Texto, covers y modelos / Alejandro con apoyo de Robert | Análisis conjunto y candidatos | Propuestas modulares, embeddings y experimentos de generación autorizados | Robert audita metodología; consulta a Andres/Santiago antes de concluir calidad musical |
| Auditoría IA / Robert | Anotaciones, baselines y resultados | Métricas reproducibles, error/interanotador, incertidumbre y preguntas difíciles | No convertir similitud en calidad pedagógica ni sustituir juicio musical |
| Interacción / Rafa | Representación revisada | Vista legible, sincronización audiovisual y tareas observables | Pedagogía valida carga cognitiva |
| Pedagogía / Alejandro + David Daza | Objetivo, nivel y material | Actividad humana, rúbrica, prueba inicial/post/diferida y consentimiento | Revisores lingüísticos contrastan dificultad y músicos revisan la tarea cantada |
| Comunidad / Alejandro | Agenda y preferencias voluntarias | Encuentros, introducciones aceptadas y continuidad | Separar participación de eficacia educativa |

Nadie debe inferir automáticamente la respuesta del otro departamento. Los artefactos
se enlazan por `study_id`, `asset_hash`, `line_id`, `candidate_id`, versión y método.
Medida ausente = null con motivo. Cada promoción a producto requiere revisión humana.

### Auditoría cruzada confirmada

Alejandro registra hipótesis/configuración y el candidato; Robert cuestiona baselines,
validez de métricas, posibles atajos, incertidumbre y derechos de datos/modelos. Si una
conclusión depende del oído, pide a Andres y Santiago evaluaciones separadas antes de
consensuar. Rafa/Alejandro contrastan la lectura japonesa; Alejandro comprueba portugués;
David/Alejandro comprueban objetivo y evaluación educativa. Documentar desacuerdos,
cambios y decisión, sin convertir al generador en su único juez.

Preguntas de Robert para los músicos: ¿el modelo mejoró inteligibilidad o solo volumen?,
¿el desplazamiento temporal es error o intención?, ¿la separación vocal introdujo
artefactos?, ¿el embedding reconoce significado o solo timbre/acompañamiento?, ¿el
candidato conserva el clímax sin forzar acentos?, ¿coinciden dos oyentes a nivel de frase?
Una revisión aprobada de música no aprueba automáticamente lengua, pedagogía ni derechos.

## 5. Herramientas: empezar pequeño

- **Ya instalado:** FFmpeg/ffprobe para inspección/conversión; Python y uv para entornos.
  Conservar el original; WAV/FLAC para intercambiar derivados y PCM sin pérdida cuando
  corresponda. No convertir permanentemente todo a mono/16 kHz: eso es un derivado
  específico para algunos modelos, no el máster de música.
- **Audacity**, si no tienen DAW: grabación, recorte y comparación de takes. Si ya usan
  Reaper, Ableton u otro, conservarlo y acordar exportación WAV + metadatos.
  [Manual de exportación](https://manual.audacityteam.org/man/wav_export_options.html).
- **Sonic Visualiser**: espectrograma y anotaciones de instantes/regiones/notas; exportar
  CSV con segundos y una referencia temporal acordada. Sirve para un baseline humano.
  [Manual](https://www.sonicvisualiser.org/doc/reference/5.0.1/en/).
- **Praat**: revisión fonética y anotaciones TextGrid por especialista. No trasladar
  ciegamente F0 de canto a pitch accent hablado; revisar errores del estimador.
  [Proyecto y manual](https://fon.hum.uva.nl/praat/).
- **librosa**, después de la anotación inicial: prototipo de onset/tempo/espectrograma
  y otras características. Fijar versiones y guardar configuración/resultados. No
  añadirlo a Android ni desplegar notebooks en la API.
  [Instalación oficial](https://librosa.org/doc/latest/install.html).

No instalar ahora todos los separadores, TensorFlow, modelos generativos y CUDA. Elegir
un baseline por problema tras conocer hardware/licencias. Código abierto, pesos abiertos
y datos de entrenamiento autorizados son comprobaciones distintas. No hay GPU mínima
necesaria para este primer flujo manual/local. No se instaló software adicional aquí.

### Capacidad confirmada y candidatos de proveedor

Alejandro confirma acceso a dos NVIDIA 4090 de la universidad, apoyo de HPC y redes
neuronales, liderazgo propio de NLP en SIMG/UNAL y algunos créditos NVIDIA/Google for
Startups. Esto es disponibilidad declarada, no acceso técnico verificado ni permiso
institucional de uso comercial. No se han conectado las GPUs ni gastado créditos.

Diseño propuesto: `Technology & AI`, `Music` y `Linguistics` como áreas operativas,
con pedagogía transversal Alejandro/David. Local-first para desarrollar experiencia
propia; nube intercambiable para pruebas/capacidad, no dependencia embebida en Android.

| Candidato | Papel razonable | Lo que no debemos suponer |
|---|---|---|
| NVIDIA Magpie TTS Multilingual | Voz hablada PT/JA para revisar texto/pronunciación, sujeto a prueba | No equivale a síntesis de canto, seguimiento de melodía ni cover fiel |
| Sakana Translate / Namazu | Análisis japonés y baseline textual, después de verificar API/condiciones | La web Translate anuncia JA/EN/ZH, no JA-PT; no asumir pesos descargables ni automatizar la web |
| Adaptador local de voz/canto por elegir | Renderizar el candidato aprobado | Elegir solo tras verificar licencia de pesos/datos/voz y prueba musical del equipo |

La ficha actual de Magpie incluye japonés y portugués brasileño y declara disponibilidad
comercial bajo términos diferenciados para modelo y contenedor. No lista RTX 4090
entre su hardware de prueba; validar compatibilidad del artefacto elegido antes de
prometer NIM local. Eso tampoco demuestra que todo su entrenamiento sea opt-in según
el estándar ético de Jikanle.
[Ficha NVIDIA](https://build.nvidia.com/nvidia/magpie-tts-multilingual/modelcard).

El checkpoint público v2607 agrega portugués; su README también contiene un campo de
aceptación «non-commercial use ONLY» junto a una referencia a Open Model License.
Resolver esta inconsistencia con el proveedor para el artefacto concreto; no aceptar
términos en nombre del equipo ni deducir permiso comercial solo del nombre de licencia.
[README del checkpoint](https://huggingface.co/nvidia/magpie_tts_multilingual_357m/blob/main/README.md).

Sakana anuncia Translate como aplicación de traducción JA/EN/ZH basada en Namazu.
Usarlo como contraste lingüístico no valida adaptación musical. Para JA-PT, comparar
un candidato directo con una revisión humana; cualquier pivote por inglés debe registrar
posibles pérdidas semánticas. El servicio web, API Namazu y un modelo local son productos
distintos; confirmar cuál se autoriza y cómo trata los textos antes de enviar letras.
[Anuncio oficial](https://sakana.ai/translate-release/).

### Ruta HPC y soberanía operativa

1. Reservar un worker por GPU para ensayos independientes antes de paralelizar un modelo.
   Dos tarjetas no se convierten automáticamente en una única memoria GPU. Registrar
   topología, driver/CUDA, RAM, almacenamiento, scheduler y ventanas de disponibilidad.
2. Contenedor/entorno por familia de modelo, versiones/checksums fijados; no instalar
   globalmente ni sustituir drivers del clúster. Respetar Slurm/Apptainer o el mecanismo
   que ya use la universidad. Las GPUs no se exponen directamente a Internet.
3. Un contrato de job/result para local y remoto, capabilities por idioma/tarea,
   presupuestos máximos, cancelación, timeout, cola, artefactos versionados y trazabilidad.
   El proveedor nunca decide unilateralmente publicar o entrenar con un archivo.
4. Benchmark privado: mismos fragmentos, configuración, semillas cuando correspondan,
   tiempos/VRAM/coste, fallos y evaluación musical/lingüística ciega cuando sea posible.
   Mantener un baseline humano y elegir por tarea, no por prestigio del proveedor.
5. Antes de un mini-datacenter colombiano/latinoamericano, medir utilización sostenida,
   energía/refrigeración, backups, red/egress, repuestos y operación. Ascenty/Claro son
   opciones mencionadas por el fundador, no servicios/cotizaciones/compatibilidad
   verificados aquí. La residencia de audio no garantiza residencia de backups/logs/API.

Pendientes de HPC: autorización de investigación y comercialización, horas/cuotas,
acceso seguro sin compartir claves por chat, titularidad de resultados, límite de gasto
y vigencia/restricciones de créditos. No enviar contenido a proveedores hasta aprobar
finalidad, retención, entrenamiento por el proveedor y derechos de entrada/salida.

## 6. Backend modular sin microservicios prematuros

### Unidad de trabajo: candidato de cover conjunto

El núcleo no debe ser «traducir primero y forzar la música después». Un
`cover_candidate` propone conjuntamente letra y decisiones musicales, y puede volver
a revisión en cualquiera de las dos áreas:

```text
texto + audio original + objetivo + límites permitidos
          -> observaciones lingüísticas y musicales
          -> candidato conjunto (letra, fraseo, acentos, respiraciones, arreglo)
          -> toma humana o motor de síntesis autorizado intercambiable
          -> evaluación por dimensiones + desacuerdos
          -> revisión del candidato -> nueva toma -> aprobación
```

Contrato propuesto: `candidate_id`, `parent_candidate_id`, versión, IDs/hash de activos,
líneas originales, texto adaptado por línea, anclajes musicales medidos, restricciones
de melodía/tempo/tonalidad/arreglo, decisiones prosódicas, artefacto de interpretación,
herramientas/modelos y rúbricas separadas. Todos los campos de tiempo no medidos quedan
null. Cambiar una letra invalida su evaluación prosódica anterior; cambiar una toma
invalida su evaluación de interpretación, pero no elimina el historial textual.

Interfaces futuras: `TextAnalyzer`, `MusicAnalyzer`, `CoverProposer`, `PerformanceRenderer`,
`CoverEvaluator`. Deben aceptar/emitir contratos, no depender de un proveedor concreto.
El primer `PerformanceRenderer` puede ser una toma de Santiago: permite evaluar el
sistema antes de comprometer derechos, presupuesto o calidad con síntesis automática.
No hay implementación de estas cinco interfaces como servicios todavía.

«Eficacia del cover» se operacionaliza como fidelidad semántica y pragmática,
naturalidad PT, ajuste acento-frase-melodía, inteligibilidad, interpretación/impacto y
respeto de cambios autorizados. Usar rúbricas independientes con revisión bilingüe y
musical; no una suma opaca de embeddings. El resultado educativo se mide aparte.
Pendiente: qué partes se pueden cambiar y qué fallos vetan un candidato.

```text
Archivo autorizado + texto revisado
  -> intake / derechos / hashes
  -> trabajo musical       -> artefactos temporales
  -> trabajo lingüístico   -> candidatos + comentarios
  -> trabajo pedagógico    -> actividad + rúbrica
  -> revisión humana conjunta
  -> contenido publicado en Supabase
  -> Web / Android: interacción, caché y presentación
```

Supabase sigue siendo Auth/Postgres/Storage y autoridad de contenido. No crear ahora
Music-Input/Music-Output/Events como servidores separados. Son límites de dominio y
carpetas/adaptadores que después podrán separarse cuando tengan ciclo de vida propio.
El código técnico actual está en Android por la organización histórica del workspace;
el siguiente traspaso debe moverlo una vez a backend/research, no copiarlo y divergir.

Prototipo backend prioritario, **aún por implementar**: tablas privadas de estudios,
activos y trabajos con dueño; URL firmada de carga/lectura de duración limitada; permisos
por finalidad; estados queued/running/needs_review/failed/approved; clave idempotente
derivada de asset+config+versión; intentos acotados, timeout y límites de tamaño/coste.
Una cola persistente y un worker Python bastan para trabajos largos de audio. No ejecutar
MIR pesado dentro de una solicitud HTTP/Edge Function. FastAPI solo cuando haga falta
un contrato que Supabase no cubra; no es un requisito del primer baseline.

Antes de activar la función textual existente: revisar quién puede modificar cada
canción, escritura atómica, cuotas y errores sin datos privados. El soporte `pt` del
adaptador TypeScript no actualiza automáticamente el CLI Python Songbridge. Su prompt
antiguo de «una nota por unidad / diferencia ±1» necesita revisión coordinada; no
representar esos conteos como validación musical.

Escalar después según evidencia: profundidad/edad de cola, latencia por etapa, coste
por minuto, tasa de fallo y revisión humana pendiente. Aumentar workers por tipo de
trabajo y capacidad real; modelos/contenido versionados permiten comparar sin rehacer
todo. Los contratos públicos no deben revelar claves, rutas locales o anotadores privados.

## 7. Pedagogía, eventos y seguridad de datos

La política completa de derechos está en
[`docs/content-rights-and-fair-training.md`](../content-rights-and-fair-training.md).
La regla operativa es opt-in específico por finalidad: análisis, adaptación, cover,
sincronización, distribución, entrenamiento y voz tienen decisiones separadas. El
generador de canciones queda bloqueado por defecto hasta que exista el ledger aprobado.

La canción sirve para un objetivo concreto, no para etiquetar universalmente al alumno.
Primera intervención propuesta: comprensión del fragmento -> explicación propia en
contexto nuevo -> feedback -> recuerdo/transferencia siete días después. La dificultad
es un vector: léxico, gramática, fonología, velocidad, claridad, cultura y afinidad.
La rúbrica/selección son humanas; no construir un generador automático de quizzes.

Para eventos, recoger intereses declarados, metas e idiomas con permisos separados
para métricas, contacto e introducciones. Dos personas deben aceptar una introducción;
no mostrar directorio ni extraer intereses de seguidores/comentarios. Guardar RSVP y
check-in por separado. Contar dos eventos del mismo idioma por semana y siguiente visita
en siete días, con denominadores/cobertura; no equivale a aprendizaje.

Los contactos con IF, Colombojaponesa, Kokomi y Sala de Idiomas BLAA permiten pedir
agendas directamente. Registrar URL oficial, permiso, ritmo semanal/mensual, zona y
revisor. El rastreador es respaldo: candidaturas pendientes, deduplicación, revisiones
de cancelación; sin inferir fechas de archivos viejos. El prototipo actual solo procesa
JSON-LD aportado por un operador, no está rastreando Instagram.

No combinar finalidad educativa con entrenamiento o Aimedic. Compartir métodos de
evaluación antes que voces/datos; investigación/entrenamiento/uso sanitario requieren
decisiones y autorizaciones independientes. Mantener trazabilidad de licencias de
composición, máster, adaptación, sincronización, intérprete y entrenamiento. La carpeta
de Drive compartida contiene documentos de trabajo, no las letras completas ni audio.

## 8. Secuencia y definición de terminado

**P0, días 1-2:** confirmar objetivo/fecha, público, revisor JA/PT y permisos; recibir
audio/intervalo. Terminado = insumos con procedencia y derechos claramente limitados.

**P0, días 3-5:** validar transcripción; glosa PT y una alternativa cantable; anotar
manualmente el mismo extracto; inspeccionar archivo; mostrar estudio en Android.
Terminado = teléfono muestra los mismos IDs/textos; discrepancias documentadas, no ocultas.

**P1, días 6-9:** Santiago graba take permitido; Andres/Robert comparan anotaciones;
Rafa sincroniza un gesto con un landmark medido; pedagogía prueba una actividad.
Terminado = comparación reproducible y evaluación cualitativa, no un score inventado.

**P1, días 10-14:** prototipo de almacenamiento privado/trabajos y lectura del estudio
revisado en el cliente; prueba con pocas personas y consentimiento; preparar video solo
si hay permisos. Terminado = replay del mismo estudio desde backend, fallos recuperables,
sin filtración entre usuarios; registrar aprendizaje y uso como métricas distintas.

En paralelo, resolver la firma/QA de Play y el onboarding de dos agendas. No prometer
que el prototipo musical, la plataforma de eventos y la publicación Play se completan
todos en dos semanas sin confirmar disponibilidad real del equipo.

## 9. Preguntas que cambian el plan

Pendientes de respuesta del fundador, no decisiones supuestas:

1. Confirmado: comparación + cover PT + video. Pendientes: fecha y criterios de aprobación.
2. Confirmado: japonés para lusófonos; comprensión/vocabulario y después producción libre. Falta nivel de entrada.
3. Tipo de audio, acceso a stems y alcance de permisos; ¿prototipo privado o lanzamiento público?
4. Confirmados: JA Alejandro/Rafa; PT Alejandro; pedagogía Alejandro/David Daza. Falta disponibilidad y revisión PT independiente cuando corresponda.
5. Confirmados: fidelidad musical Andres/Santiago; texto/covers/IA Alejandro con Robert como apoyo/auditor. Faltan horas por semana.
6. Confirmadas dos 4090 institucionales y créditos; enfoque local-first. Faltan SO/DAW, scheduler/acceso, autorización comercial, cuotas y tope de gasto.
7. Identidad: Acervo Labs interno o marca pública; quién aprueba contenido/investigación.
8. Primer cohort: tamaño, adultos/menores y evidencia deseada; no empezar con datos de menores sin protocolo.
9. Qué documentos/datasets podrán compartirse con el equipo y cuáles requieren acceso restringido.
10. Lista de instituciones, URLs oficiales, contacto por referencia privada y cadencia acordada.

## 10. Documentación y sincronización

Destino solicitado: `Jikanle_Business/Docs/Research-Product/`. Documento general ahí;
contratos/tests técnicos permanecen junto al código. `STATUS.md` debe señalar cambios
de contrato para Web/backend/Songbridge. No duplicar la fuente canónica de SQL.

Drive solicitado: carpeta Jikanle, ID `1LOl27f03WHNtQk0xFITxau5nmXkzHWC2`, con una
subcarpeta `Research-Product`. Subir solo documentos preparados; no `rclone sync` del
workspace completo porque puede borrar/propagar archivos privados. Hay remotos llamados
`gdrive:` y `gdrive-unal:`, pero su correspondencia no está verificada. Una copia puntual
verificada no es una sincronización automática continua. No cambiar permisos de carpeta.

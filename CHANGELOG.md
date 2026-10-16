# GUI Tweaks — historial de versiones

Esquema: 0.1, 0.2, 0.3... hasta que sea estable; entonces será 1.0.

## 0.14.0
- Vistas guardadas: seis ranuras (General → "Vistas guardadas...") con nombre que elige el jugador. Cada ranura guarda la vista completa: ajustes de apariencia, posiciones de los paneles, opacidad, escala, acento, etc. Se puede cargar, guardar, duplicar (a la primera ranura libre) y borrar, y todo lo que sobrescribe o borra pide confirmación.
- Al cargar una ranura con cambios sin guardar en la vista actual, avisa antes de perderlos; "Deshacer carga" devuelve la vista anterior mientras no se cierre el juego. La pantalla indica qué vista está activa y si tiene cambios sin guardar.
- Tecla "Siguiente vista guardada" (Opciones → Controles → GUI Tweaks; sin asignar por defecto) para pasar a la siguiente ranura ocupada sin abrir menús.
- Grupos de comandos rápidos: hasta 3 grupos con nombre (por ejemplo, uno por servidor), con pestañas en "Editar comandos...". En el chat, un botón largo sobre los botones rápidos alterna entre grupos (solo aparece si hay más de uno). Los comandos ya no forman parte de las vistas guardadas. Al actualizar desde una versión anterior, tus comandos pasan al primer grupo ("Principal").
- Pantalla de ajustes reorganizada por temas en cinco pestañas: Estado (panel de vida y hambre), Brújula (info, FPS, coordenadas), Barra (barra de items, equipo, efectos, aviso del item), Mobs y General (acento, escala, opacidad, botón de la pausa, estela de flecha, editor de paneles, comandos del chat y vistas guardadas, y el reinicio total). "Pestaña: valores por defecto" restablece solo la pestaña abierta.
- Editor de paneles: vuelve a un solo botón, "Deshacer cambios". El botón "Listo", "Deshacer cambios", la ayuda y el rótulo de la zona reservada caben ahora dentro de la zona reservada, así que ya no tapan paneles ni se pisan con sus rótulos; los botones se dibujan por encima de la cuadrícula y de la zona.
- "Restablecer TODO el mod" (pestaña General) también borra los grupos de comandos y las vistas guardadas, y su aviso lo dice.
- Compatible con Minecraft 26.2 y 26.3 (el mismo jar sirve en las dos). En la 26.3 Mojang cambió el tipo de tecla y el paquete de la clase de dibujo de sprites; los iconos de vida, hambre, armadura, aire, montura y equipo se dibujan ahora buscando esa clase al primer uso, y si no se encuentra solo faltan los iconos y se avisa en el log, sin cerrar el juego.
- Valores de fábrica más compactos para una mejor primera impresión: panel de estado a la izquierda, escala 65 %, barras de 65 px y equipo junto a la barra de items. Solo afecta a instalaciones nuevas y a "restablecer"; la configuración guardada no cambia.

## 0.13.1
- Correcciones sobre la 0.13.0, sin funciones nuevas.
- Editor de paneles: ahora hay dos botones. "Deshacer cambios" devuelve los paneles a la posición que tenían al abrir el editor (queda apagado si no hay nada que deshacer). "Posiciones por defecto" los devuelve a la colocación automática sin tocar el resto del mod, y se puede deshacer mientras el editor siga abierto. Los dos piden confirmación.
- "Restablecer todo" (que sí borraba toda la configuración del mod) sale de la barra inferior, que aparece en todas las pestañas, y pasa a la pestaña Extras, con la advertencia en rojo dentro del propio botón, una explicación debajo y una confirmación. La barra inferior queda con "Listo" y "Pestaña: valores por defecto" (antes "Restablecer pestaña"), que solo devuelve a su valor de fábrica los ajustes de la pestaña abierta.

## 0.13.0
- Editor de paneles: un panel ya no puede pisar a otro. Si el movimiento chocaría, avanza hasta donde pueda y se desliza a lo largo del obstáculo. Un panel que ya estaba pisando a otro (de una configuración anterior) puede moverse mientras no pise más.
- Editor de paneles: zona reservada abajo, marcada en rojo, que ningún panel puede ocupar: la barra de items y, justo encima, las barras de aire, salto y vida de la montura.
- Editor de paneles: cuadrícula guía 6x6 sobre la pantalla (las líneas del centro, más marcadas).
- "Restablecer posiciones" ahora pide confirmación antes de borrar, y no hace nada si no hay posiciones guardadas.

## 0.12.0
- Editor de paneles: Extras → "Editar posición de paneles..." (también desde el botón GUI Tweaks de la pausa). Los paneles se ven sobre el juego, con datos de muestra si hace falta (efectos, vida de mobs), y se arrastran con el ratón; las flechas afinan de 1 en 1 px (Mayús: 5 px). Atrae al centro de la pantalla, no deja sacar un panel del todo, y "Restablecer posiciones" devuelve todo a la colocación automática.
- Se pueden mover: panel de estado, brújula/info, efectos, armadura (perfil de arriba), las dos columnas de equipo junto a la barra y la tarjeta de vida de mobs. Cada posición se guarda como desplazamiento sobre la colocación automática, así que sigue adaptándose al cambiar de pantalla o escala.

## 0.11.1
- Los puntitos de jugadores del localizador vanilla, que seguían saliendo justo sobre la barra de items, ya no se dibujan mientras las barras vanilla estén ocultas. Causa: el juego pinta la barra de información (XP, localizador o salto) en dos llamadas, y la de los puntos queda fuera de lo que Fabric permite ocultar. Ahora, mientras "Ocultar barras vanilla" está activo, el juego usa una barra vacía en lugar del localizador (se cambia por reflexión, sin Mixins) y la restaura al desactivarlo. La brújula del mod ocupa su lugar. Si el cambio no se pudiera aplicar, el mod lo avisa en el log sin afectar al resto.

## 0.11.0
- Brújula: con "Cabezas de jugador" activado ya no se dibujan puntos de color, solo cabezas (las que quedan al borde de la vista se ven atenuadas). Con la opción desactivada se usan puntos.
- Brújula: "Bajar brújula" ahora es un porcentaje (0 a 100 %) y llega casi hasta abajo, justo por encima de la zona de la barra de salto, la de aire y la vida de la montura. Sustituye al ajuste anterior en píxeles.
- Brújula: más espacio vertical en la fila de cabezas y una flechita ▲ o ▼ cuando el jugador está más de 3 bloques por encima o por debajo de ti. Solo para jugadores cargados cerca: de los lejanos el juego no envía la altura.

## 0.10.5
- La barra de aire usa el mismo grosor (3 px), ancho (120 px) y posición que la barra de salto de la montura, para que ambas se vean homogéneas (nunca se usan a la vez). El icono de burbuja queda a su izquierda.

## 0.10.4
- Vida de la montura (los iconos que el juego pone a la derecha sobre la barra de items): ahora va centrada sobre la barra de salto, un 25 % más pequeña, y usa los mismos iconos del juego (siguen el texture pack). Se oculta la vanilla junto a las demás barras.
- Barra de salto más larga (120 px) con el mismo grosor.

## 0.10.3
- Barra de salto de la montura mucho más corta y fina (60x3 px), pegada a la barra de items. La barra de aire vuelve a su tamaño anterior (90x6 px, la 0.10.2 la había achicado de más).

## 0.10.1
- Corrección: la 0.10.0 exigía Fabric API 0.161.0 o superior (la versión contra la que se compila) y no cargaba con la 0.160.0. El mínimo vuelve a ser 0.160.0, la versión con la que se ha probado el mod.

## 0.10.0 (candidata a 1.0)
- Barra de salto de la montura (caballo, mula…) con el estilo del mod: al ocultar las barras vanilla se perdía; ahora se dibuja sobre la barra de items, con aviso de recarga.
- Barra de aire bajo el agua en el estilo del mod (icono y píldora que se vacía, con aviso rojo al quedar poco).
- Barra de items del mod: animación de "salto" al recoger items e indicador de ataque cuando el juego lo tiene en la barra (la columna de equipo se aparta para no taparlo).
- Los paneles reducen su escala por sí solos si no caben en pantallas o escalas de GUI pequeñas.
- Configuración: botón "Restablecer pestaña" además de "Restablecer todo".
- `fabric.mod.json`: Fabric API 0.161.0 o superior como mínimo.
- Licencia MIT en el archivo `LICENSE` (antes quedaba el CC0 de la plantilla) y README con descripción, requisitos y nota de transparencia sobre el uso de IA.

## 0.9.1
- Estela de flecha: ahora colisiona con mobs y jugadores, como la flecha del juego, en vez de atravesarlos; el objetivo al que daría se marca con un borde muy fino del color de acento.
- Punto de impacto mucho más visible incluso lejos: aro que crece con la distancia, punto, mástil y trazo más grueso, con puntos cada 4 ticks para apreciar la caída desde primera persona (donde la curva se ve como una línea vertical).

## 0.9.0
- (Experimental) Estela de flecha (Extras → "Estela de flecha"), desactivada por defecto: al tensar un arco, o con una ballesta cargada, se dibuja en el mundo la trayectoria que seguiría la flecha, con el color de acento y desvaneciéndose con la distancia, y una marca en el punto donde chocaría con un bloque. Simula la física de la flecha del juego (gravedad, rozamiento, agua, tu propio movimiento) y se apoya en el sistema de "gizmos" del juego. Algunos servidores consideran estas ayudas de puntería como trampa: usar con cuidado.

## 0.8.0
- Cabeza 3D opcional (pestaña Panel de estado → "Cabeza 3D", desactivada por defecto): el modelo real del jugador, con su skin, la capa del sombrero y el casco puesto, dibujado con el renderizador del inventario y recortado para mostrar solo la cabeza. Gira hacia el lado al mover el ratón y vuelve poco a poco al centro, y se inclina según miras arriba o abajo. Con sliders de zoom y altura para ajustar el encuadre.
- Nuevo tercer estilo de info, "Brújula circular": dial redondo cuya rosa de los vientos gira al moverte, con rumbo en grados en el centro, puntero fijo arriba y los jugadores sobre el anillo (cabeza o punto). FPS y coordenadas debajo.

## 0.7.0
- Brújula plana arriba al centro (nuevo "Estilo de info: Brújula", activado por defecto; el estilo "Línea" sigue disponible): tira con N, NE, E, SE, S, SO, O, NO que se desplaza al girar, con marcas cada 15 grados y un puntero central.
- Marcadores de otros jugadores sobre la brújula, con su cabeza (o un punto de color) en la dirección en que están. Usa el sistema de localizador del juego, así que incluye jugadores lejanos si el servidor los envía; si no, usa los jugadores cercanos. Los que quedan fuera de la vista se pegan al borde, atenuados. Los lejanos se ven más tenues.
- FPS y coordenadas pasan a una línea centrada bajo la brújula.
- Nuevas opciones (Info y efectos): estilo de info, jugadores en la brújula, cabezas o puntos, alcance (50 a 1000 bloques) y "Bajar brújula" (para no pisar la barra de jefe).

## 0.6.0
- Botón "GUI Tweaks" en la esquina superior derecha de la pantalla de pausa (Esc) que abre la configuración sin depender de Mod Menu. Mismo estilo del mod, con un icono de controles deslizantes. Se puede quitar con la opción "Botón en la pausa" (pestaña Extras).
- Los botones del chat y el de la pausa comparten ahora el mismo dibujo de caja (`Draw.buttonBox`), para que el estilo sea uniforme.

## 0.5.1
- Barra de vida de mobs: la tarjeta ya no se queda tras dejar de apuntar al mob (antes 0,8 s de espera más 0,4 s de desvanecido; ahora 0,12 s más 0,22 s), para que no llene la pantalla al pasar la vista por varios mobs seguidos.

## 0.5.0
- Barra de vida de mobs: al apuntar a un mob dentro de la distancia configurada (4 a 64 bloques), aparece bajo la cruz una tarjeta pequeña con su nombre, una barra en píldora (verde a rojo según la vida) y el valor. No se ve a través de bloques, se mantiene un instante al dejar de apuntar y se desvanece. La vida mostrada se acerca suavemente a la real.
- Nueva pestaña "Mobs" en la configuración: activar, distancia y filtros de hostiles, no hostiles y jugadores (estos últimos desactivados por defecto).
- Botones del chat más compactos: los nombres largos ya no ensanchan la caja; el texto se desliza de un extremo al otro con una pausa en cada uno.

## 0.4.1
- Panel de comandos del chat rediseñado: caja con el estilo del mod (opacidad y color de acento), botones pequeños en cuadrícula de 3 o 4 por fila (hasta 4 en una fila; 5-6 comandos en filas de 3), texto reducido si no cabe y resalte con el color de acento al pasar el ratón. Con opacidad 0 solo se ve el resalte.
- Nueva opción "Panel del chat": Centro (por defecto) o Arriba.

## 0.4.0
- Opacidad 0 = totalmente transparente: ya no se dibuja ni el borde ni la línea de acento, y el fondo tenue de las columnas de equipo y el panel del chat también siguen la opacidad. Los iconos "flotan".
- Nueva opción "Barra de items" (Info y efectos): Vanilla o Estilo del mod. La barra del mod usa el mismo fondo, color de acento y opacidad que los demás paneles, con la casilla seleccionada siempre visible aunque el fondo sea transparente, y la mano secundaria en su propio recuadro. Los items se dibujan con el renderizador del juego, así que siguen el texture pack. Por defecto sigue siendo la vanilla.

## 0.3.0
- Equipo con los iconos nativos de los items (y su barra de durabilidad del juego) en los dos perfiles, en lugar de siluetas. Las piezas que no se llevan muestran la ranura vacía del inventario.
- Perfil "Junto a la barra": iconos del mismo tamaño que los de la hotbar. Se quitó la ranura de la mano izquierda (el juego ya la dibuja): casco, pechera y flechas quedan apilados justo encima de ella. A la derecha: mano derecha, pantalón, botas y espacios libres.
- Perfil "Armadura (arriba)": las cuatro piezas con iconos reducidos, alineadas con la altura del panel de estado.

## 0.2.2
- Perfil de equipo "Junto a la barra" mucho más pequeño: items a 10 px, siluetas de armadura a la mitad, filas bajas y sin recuadro; solo un fondo muy tenue del color de acento.

## 0.2.1
- Perfil de equipo "Armadura (arriba)": ahora solo muestra las piezas de armadura flotando junto al panel de estado, sin recuadro y sin los items de las manos.

## 0.2.0
- Panel de equipo con dos perfiles (se elige en Info y efectos → Equipo; apagado por defecto):
  - Muñequito junto al panel de estado: casco, pechera o élitros, pantalón y botas apilados, con el item de la mano izquierda a un lado y el de la derecha al otro.
  - Columnas a los lados de la barra de items: izquierda (mano izquierda, casco, pechera o élitros, flechas), derecha (mano derecha, pantalón, botas, espacios libres del inventario).
- Las piezas de armadura son siluetas del color del material vanilla (cuero teñido, cota, hierro, oro, diamante, netherita, tortuga, cobre) que se apagan hacia gris al desgastarse.

## 0.1.0
Primera versión guardada (Minecraft 26.2, Fabric, solo cliente).

- Panel de estado con cabeza del jugador, barras de vida, comida, armadura y XP en forma de píldora.
- Vida por capas (x2, x3...) cuando se tiene más de 20, absorción como línea dorada.
- Animaciones de la cabeza: fuego, golpe, veneno, wither, congelación, poca vida, flechas clavadas y burbujas de aire.
- Línea de info (FPS, XYZ, dirección) debajo del panel.
- Panel de efectos de poción con icono, nivel y tiempo restante.
- Aviso al cambiar de item: nombre, encantamientos y barrita de durabilidad.
- Comandos rápidos en el chat (T o /), configurables con sugerencias.
- Pantalla de configuración en Mod Menu, con pestañas.

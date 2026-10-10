# GUI Tweaks — historial de versiones

Esquema: 0.1, 0.2, 0.3... hasta que sea estable; entonces será 1.0.

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

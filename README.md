# GUI Tweaks

> 🤖 **Proyecto desarrollado con asistencia de IA.** El código fue escrito en gran parte por IA, bajo la dirección de
> [ccjoshua2813](https://github.com/ccjoshua2813), que diseñó las funciones, tomó las decisiones y probó cada versión
> en el juego. La descripción también se redactó con ayuda de IA. Los commits lo indican con la línea `Assisted-by`.

Mod de **solo cliente** para Minecraft **26.2** (Fabric) que rediseña la interfaz con un estilo uniforme:
mismo color de acento, misma opacidad y todo configurable. Con la opacidad en 0 los paneles desaparecen y los
iconos flotan.

## Qué incluye

- **Panel de estado:** cabeza del jugador (plana o 3D que gira contigo) con barras de vida, comida, armadura y XP
  en forma de píldora; vida por capas (x2, x3…) y animaciones de fuego, golpe, veneno, wither, congelación,
  poca vida, flechas clavadas y aire.
- **Brújula** (plana o circular) con otros jugadores, y FPS y coordenadas debajo; o la línea de info clásica.
- **Panel de efectos** de poción con icono, nivel y tiempo restante.
- **Equipo:** armadura y manos con los iconos del juego (dos perfiles de colocación).
- **Barra de items** con el estilo del mod (opcional), con la animación de recoger items y el indicador de ataque.
- **Aviso al cambiar de item:** nombre, encantamientos y una barrita de durabilidad.
- **Vida de mobs** al apuntarles, con distancia y filtros (hostiles, no hostiles, jugadores).
- **Comandos rápidos** en el chat: una caja de botones que defines tú, en hasta 3 grupos con nombre (por ejemplo, uno por servidor) entre los que alternas con un botón.
- **Estela de flecha** (experimental): trayectoria de la flecha al apuntar con un arco o una ballesta cargada.
- Barras de **salto de la montura** y de **aire** con el mismo estilo.
- **Editor de paneles:** arrastra cada panel a donde quieras, con cuadrícula guía, sin que se pisen entre sí y sin invadir la zona de la barra de items.
- **Vistas guardadas:** seis ranuras con el nombre que quieras para guardar y cambiar entre distintas configuraciones de apariencia y posiciones.
- **Configuración** con pestañas desde Mod Menu o desde el botón "GUI Tweaks" de la pantalla de pausa (Esc).

## Requisitos

- Minecraft 26.2 o 26.3, Fabric Loader 0.19.5 o superior, Java 25.
- [Fabric API](https://modrinth.com/mod/fabric-api) (obligatorio).
- [Mod Menu](https://modrinth.com/mod/modmenu) (opcional).

No usa Mixins ni access wideners.

## Aviso sobre la estela de flecha

Es una ayuda de puntería. Algunos servidores la consideran trampa: está desactivada por defecto y marcada como
experimental. Úsala solo donde esté permitido.

## Compatibilidad

Si usas otro mod que dibuja lo mismo (barra de vida de mobs, armadura, equipo, brújula), verás elementos
duplicados. Desactiva uno de los dos.

## Transparencia sobre el uso de IA

El código fue escrito en gran parte por IA, bajo la dirección de ccjoshua2813, que diseñó las funciones, tomó las decisiones y probó cada versión en el juego. La descripción también se redactó con ayuda de IA. Se indica con la línea `Assisted-by` en los commits, aquí y en cada release, para ser transparentes sobre cómo se hizo.

## Licencia

[MIT](LICENSE).

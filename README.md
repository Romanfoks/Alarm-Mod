![Alarm Mod](https://cdn.modrinth.com/data/cached_images/fb9749821de5989efdae30c6b3dfddf4a69b88aa_0.webp)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/alarm-mod?style=flat-square&color=00AF5C)](https://modrinth.com/mod/alarm-mod)
[![GitHub Issues](https://img.shields.io/github/issues/Romanfoks/Alarm-Mod?style=flat-square)](https://github.com/Romanfoks/Alarm-Mod/issues)

**Alarm Mod** adds an RGB siren block to Minecraft. The siren is a real electrical device
on the **Create: Power Grid** network: it has four contacts, and the colour comes out of how
you wire it. Light is rendered with the **Veil** engine.

---

## ✨ Features

* **Four contacts:** red, green, blue and a shared ground, on the mounting face.
* **Wiring makes the colour:** each channel is a resistor to the common ground, so the
  siren is literally an LED with a shared cathode. Power only R — red. R and G — yellow.
  All three — white. No GUI, no dyes.
* **Analog brightness:** channel level follows the voltage across it, so a rheostat or
  a step-down transformer dims the siren smoothly instead of switching it.
* **Directional placement:** the siren mounts on any face and aims its light away from
  that surface.
* **Veil rendering:** ambient glow plus two sweeping light cones, coloured by the
  channels themselves.

## 🔌 Requirements

| Mod | Why |
| --- | --- |
| Minecraft 1.21.1 | |
| NeoForge 21.1+ | |
| [Create 6](https://modrinth.com/mod/create) | block entities, rendering |
| [Create: Power Grid](https://modrinth.com/mod/power-grid) | the electrical network the siren runs on |
| [Veil](https://modrinth.com/mod/veil) | light rendering |
| Sable + Sable Companion | sub-level support for light positions |

My russian guide https://www.youtube-nocookie.com/embed/uxXfl7dBp5g

---

## 🧪 Craft chain

Lapis is milled into dust, mixed into **Lazurite Acid**, reduced with redstone into
**Redstone Acid**, combined with iron into **Redstone Iron**, and magnetised with Power Grid
into **Redstone Charged Iron**. Nine of those craft the block.

---

## 🔨 Building

```bash
./gradlew build
```

Requires a JDK 21 toolchain. Gradle 9.2.1 cannot run on JDK 26 — if `JAVA_HOME` points at a
newer JDK, the build fails with `Unsupported class file major version 70`.

The build also pulls **Ponder** and **Flywheel** from `maven.createmod.net` for compilation
only. Neither is put on the runtime classpath, because Create already ships both.

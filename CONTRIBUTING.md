# Cómo trabajamos en EventHub

## La regla principal

**Nadie sube cambios directo a `main`.** `main` es la versión oficial del proyecto: lo que está ahí es lo que vamos a entregar y mostrar en la sustentación. Todo cambio entra por un **pull request (PR)** que otro integrante revisa y aprueba.

¿Por qué? Porque así:
- Siempre hay un segundo par de ojos sobre cada cambio.
- Si algo se rompe, sabemos qué PR lo introdujo.
- Queda evidencia en GitHub de que trabajamos en equipo (el profesor la puede ver).

## El ciclo completo

```
1. Tomo un issue  →  2. Creo mi rama  →  3. Trabajo y hago commits
→  4. Subo la rama  →  5. Abro el PR  →  6. Alguien revisa
→  7. Corrijo si hace falta  →  8. Merge  →  9. Borro la rama
```

### 1. Toma un issue

En el tablero del proyecto, asígnate el issue y muévelo a **En progreso**. Cada rama corresponde a **un solo issue**.

### 2. Crea tu rama desde `main` actualizado

```bash
git switch main
git pull
git switch -c feat/23-compra-concurrencia
```

**Nombres de rama:** `tipo/numero-del-issue-descripcion-corta`

| Tipo | Cuándo se usa | Ejemplo |
|---|---|---|
| `feat/` | Funcionalidad nueva | `feat/24-generacion-qr` |
| `fix/` | Corrección de un error | `fix/24-qr-doble-lectura` |
| `docs/` | Documentación, diagramas, ADRs | `docs/15-contratos-rabbitmq` |
| `infra/` | Docker, configuración | `infra/8-docker-compose` |

### 3. Trabaja y haz commits pequeños

```bash
git add .
git commit -m "feat(boleteria): descontar inventario con update condicional"
```

Un commit = un cambio que se puede explicar en una línea. Mejor varios commits pequeños que uno gigante al final.

### 4. Sube tu rama (no `main`)

```bash
git push -u origin feat/23-compra-concurrencia
```

### 5. Abre el PR

1. Entra al repo en GitHub. Aparece un aviso amarillo con el botón **Compare & pull request**. Si no aparece, ve a la pestaña **Pull requests → New pull request**.
2. Verifica que diga `base: main ← compare: tu-rama`.
3. Llena la plantilla que aparece. En la descripción escribe `Closes #23` (con el número de tu issue): así el issue se cierra solo al hacer el merge.
4. A la derecha, en **Reviewers**, agrega a un compañero.

> Si todavía no terminas pero quieres que vean cómo vas, abre el PR como **borrador** (flecha junto al botón → *Create draft pull request*).

### 6. Revisar el PR de un compañero

1. Abre el PR y entra a la pestaña **Files changed**.
2. Si algo no se entiende o está mal, haz clic en el **+** azul junto a la línea y deja un comentario.
3. Al terminar, presiona **Review changes** y elige:
   - **Approve:** todo bien.
   - **Request changes:** hay algo que corregir antes de unirlo.
   - **Comment:** solo preguntas o sugerencias.

**Cómo comentar:** sobre el código, no sobre la persona. "Esta consulta podría vender de más si dos compras llegan al tiempo" en vez de "esto está mal".

**Tiempo de respuesta:** revisamos los PR el **mismo día** que nos los asignan. Un PR esperando bloquea al compañero.

### 7. Si te piden cambios

No abras otro PR. Haz los cambios en **la misma rama**, haz commit y `git push`. El PR se actualiza solo. Luego responde los comentarios y avisa que ya está.

### 8. Merge

Con la aprobación, el **autor** del PR presiona **Merge pull request**. Usamos **Squash and merge**: junta todos los commits del PR en uno solo, así `main` queda limpio.

### 9. Borra la rama y actualiza tu `main`

En GitHub presiona **Delete branch**. En tu computador:

```bash
git switch main
git pull
git branch -d feat/23-compra-concurrencia
```

## Problemas comunes

**"Tengo cambios en `main` y no en mi rama"**
Si todavía no hiciste commit, crea la rama ahora mismo; los cambios se van contigo:
```bash
git switch -c feat/23-compra-concurrencia
```

**"GitHub dice que mi PR tiene conflictos"**
Alguien cambió las mismas líneas en `main`. Trae los cambios a tu rama:
```bash
git switch main
git pull
git switch feat/23-compra-concurrencia
git merge main
```
Abre los archivos marcados, deja la versión correcta, borra las marcas `<<<<<<<`, `=======` y `>>>>>>>`, y haz commit. Si no estás seguro de cuál versión dejar, pregúntale a quien hizo el otro cambio.

**"No puedo hacer push a `main`"**
Está bien: `main` está protegida a propósito. Sube tu rama y abre un PR.

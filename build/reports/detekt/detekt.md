# detekt

## Metrics

* 8,156 number of properties

* 3,149 number of functions

* 1,319 number of classes

* 218 number of packages

* 880 number of kt files

## Complexity Report

* 99,144 lines of code (loc)

* 61,031 source lines of code (sloc)

* 41,132 logical lines of code (lloc)

* 22,327 comment lines of code (cloc)

* 11,060 cyclomatic complexity (mcc)

* 4,758 cognitive complexity

* 2 number of total code smells

* 36% comment source ratio

* 268 mcc per 1,000 lloc

* 0 code smells per 1,000 lloc

## Findings (2)

### naming, InvalidPackageDeclaration (1)

Kotlin source files should be stored in the directory corresponding to its package statement.

[Documentation](https://detekt.dev/docs/rules/naming#invalidpackagedeclaration)

* C:/Users/teeja/Desktop/zephyr/src/main/kotlin/net/ccbluex/liquidbounce/features/module/modules/Category.kt:19:1
```
The package declaration does not match the actual file location.
```
```kotlin
16  * You should have received a copy of the GNU General Public License
17  * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
18  */
19 package net.ccbluex.liquidbounce.features.module
!! ^ error
20 
21 enum class Category(val readableName: String) {
22 

```

### style, NewLineAtEndOfFile (1)

Checks whether files end with a line separator.

[Documentation](https://detekt.dev/docs/rules/style#newlineatendoffile)

* C:/Users/teeja/Desktop/zephyr/src/main/kotlin/net/ccbluex/liquidbounce/features/module/modules/combat/tpaura/ModuleTpAura.kt:90:2
```
The file C:\Users\teeja\Desktop\zephyr\src\main\kotlin\net\ccbluex\liquidbounce\features\module\modules\combat\tpaura\ModuleTpAura.kt is not ending with a new line.
```
```kotlin
87 open class TpAuraChoice(name: String) : Choice(name) {
88     override val parent: ChoiceConfigurable<TpAuraChoice>
89         get() = ModuleTpAura.mode
90 }
!!  ^ error

```

generated with [detekt version 1.23.6](https://detekt.dev/) on 2025-01-23 14:42:06 UTC

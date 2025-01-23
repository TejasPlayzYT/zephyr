# detekt

## Metrics

* 8,154 number of properties

* 3,149 number of functions

* 1,317 number of classes

* 218 number of packages

* 880 number of kt files

## Complexity Report

* 99,143 lines of code (loc)

* 61,026 source lines of code (sloc)

* 41,127 logical lines of code (lloc)

* 22,324 comment lines of code (cloc)

* 11,059 cyclomatic complexity (mcc)

* 4,758 cognitive complexity

* 1 number of total code smells

* 36% comment source ratio

* 268 mcc per 1,000 lloc

* 0 code smells per 1,000 lloc

## Findings (1)

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

generated with [detekt version 1.23.6](https://detekt.dev/) on 2025-01-23 14:45:58 UTC

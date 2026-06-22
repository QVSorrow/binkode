# Bincode

[![](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)

Kotlin implementation of [bincode](https://github.com/bincode-org/bincode) protocol
via [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)

# NOTE: This library in early development stage

- Some APIs may change frequently
- `Bincode` implementation of `kotlinx.serialization.BinaryFormat` have a stable API
- The library expected to be fully compatible with rust bincode implementation.

## Installation

Available on Maven Central. Add it to your Gradle build:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.qvsorrow:binkode:0.1.0")
}
```

## Usage

### Binary encode / decode

```kotlin
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import me.qvsorrow.binkode.Bincode

@Serializable
data class Point(val x: Int, val y: Int)

val bytes = Bincode.encodeToByteArray(Point(1, 2))
val point = Bincode.decodeFromByteArray<Point>(bytes)
```

### Streaming with Okio

```kotlin
import me.qvsorrow.binkode.Bincode
import me.qvsorrow.binkode.decodeFromSource
import me.qvsorrow.binkode.encodeToSink
import okio.Buffer

val buffer = Buffer()
Bincode.encodeToSink(buffer, Point(1, 2))
val point = Bincode.decodeFromSource<Point>(buffer)
```

### Custom configuration

```kotlin
import me.qvsorrow.binkode.Bincode
import me.qvsorrow.binkode.ByteEndian
import me.qvsorrow.binkode.IntEncoding

val bincode = Bincode {
    endian = ByteEndian.BigEndian
    intEncoding = IntEncoding.Fixed
}
```

### Sealed types

Sealed subtypes require an explicit serial name built from `SEALED_TAG`:

```kotlin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.qvsorrow.binkode.SEALED_TAG

@Serializable
sealed interface Shape {
    @SerialName("$SEALED_TAG;0")
    @Serializable
    data class Circle(val radius: Double) : Shape

    @SerialName("$SEALED_TAG;1")
    @Serializable
    data class Square(val side: Double) : Shape
}
```

### From Bincode FAQ:
The encoding format is stable, provided the same configuration is used. This should ensure that later versions can still read data produced by a previous versions of the library if no major version change has occurred.

Bincode 1 and 2 are completely compatible if the same configuration is used.

Bincode is invariant over byte-order, making an exchange between different architectures possible. It is also rather space efficient, as it stores no metadata like struct field names in the output format and writes long streams of binary data without needing any potentially size-increasing encoding.

As a result, Bincode is suitable for storing data. Be aware that it does not implement any sort of data versioning scheme or file headers, as these features are outside the scope of this crate.

# TODOs

- [x] @Serializable class
- [x] Unsigned types
- [x] List
- [x] Map
- [x] Nullable 
- [ ] Sealed (supported, but requires custom serial name)
- [x] value class 
- [x] streaming support via Okio Source/Sink

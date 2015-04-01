# clj-amp

A Clojure implementation of the Asynchronous Messaging Protocol, or
[AMP](http://amp-protocol.net/).

## Status

[![Build Status](https://travis-ci.org/mithrandi/clj-amp.svg?branch=master)](https://travis-ci.org/mithrandi/clj-amp)

## Usage

### A note about "byte strings"

The AMP protocol uses sequences of arbitrary bytes in many places that a
human-readable identifier is expected. While some languages have a convenient
way to express such sequences of bytes with a constant (for example, `b'foo'`
in Python), Clojure lacks such a thing. In their place, clj-amp makes use of
regular strings (which are just the Java String type, normally used to
represent sequences of Unicode codepoints); these strings are translated to
bytes by way of ISO-8859-1 encoding, which has the convenient property that all
encodings are 1-byte encodings, and furthermore the first 256 Unicode
codepoints map precisely to the 256 possible values for a byte.

This means that you can write "foo" in the places in clj-amp where you would
expect to be able to; you can also express arbitrary sequences of bytes by
using a string with the appropriate codepoints. For example, if you needed to
express the sequence `01 FF 1A`, this would be written as the string
`"\u0001\u00FF\u001A"`.

It is expected that these values will be written as constants in almost all
cases; at the moment, there are no helpers for converting from other
representations, but these may be added if demand for them presents itself.

When reference is made to a "byte string" elsewhere in the documentation, this
is the construct to which it refers.

### Defining commands

#### defcommand

A `defcommand` macro is provided for command definitions:

```clojure
(defcommand name
 {argument-name {:type argument-type
                 :name amp-argument-name?
                 ...}
  ...}
 {response-name {:type argument-type
                 :name amp-response-name?
                 ...}
 :command-name command-name?
 :errors {::error-type \"ERROR-CODE\"
          ...})
```

The arguments and response items are required, but you can pass an empty map if
necessary. Responders are passed a map of arguments, and are expected to return
a map in response; the keys are the same as specified in the command
definition.

By default, the protocol-level name will be derived from the Clojure-level
name, but you can pass :command-name with a byte string to override the command
name itself, or :name with a byte string to override an argument or response
item name.

An errors mapping can also be provided; the default is an empty mapping. The
key in the errors mapping will be used for the :type item in an ex-info
exception; the value is the protocol-level identifier for the error. If you
need to handle non-ex-info exceptions, you will need to catch these in your
responder and rethrow as an ex-info exception.

#### Argument types

Argument types are specified by qualified keywords. All of the built-in
argument types are defined in `clj-amp.argument`, so you can either address
them as `::clj-amp.argument/some-type` or import the namespace under an alias:
for example, `:require [clj-amp.argument] :as a` and then `::a/some-type`. This
also makes it easier to define your own argument types without worrying about
clashing with built-in types or custom types from elsewhere.

Argument encoding/decoding is handled by two sets of multimethods in
`clj-amp.argument`: `to-bytes` / `from-bytes`, and `to-box` / `from-box`.
Argument types for which the `-bytes` methods are defined can only be encoded
as a single box item, which allows them to be used in composite structures such
as `::list`.

The primitive types are as follows:

- `::integer`
  An arbitrary-size integer; equivalent to `Integer` in the reference
  implementation.

  WARNING: Currently represented solely as a `Long`, thus values outside of
  `[Long.MIN_VALUE, Long.MAX_VALUE]` cannot currently be handled.

- `::string`

  A sequence of Unicode codepoints; equivalent to `Unicode` in the reference
  implementation. Represented as a Clojure (Java) `String`.

- `::byte-string`

  A sequence of bytes; equivalent to `String` in the reference implementation.
  Represented as a Clojure (Java) `String`; see the earlier section for more
  information.

- `::bytes`

  A sequence of bytes; also equivalent to `String` in the reference
  implementation. Represented as the native Gloss representation (a sequence of
  byte buffers); this allows further use of the bytes (eg. writing them to a
  file) without incurring the cost of additional copying. You may also find
  `gloss.io/to-byte-buffer` and `byte-streams/convert` of use when working with
  these values. For encoding, any type that Gloss / byte-streams can convert to
  bytes is accepted.

- `::float`

  An IEEE 754 binary floating-point number; equivalent to `Float` in the
  reference implementation. Represented as a Clojure (Java) `Double`.

- `::decimal`

  A decimal floating-point number; equivalent to `Decimal` in the reference
  implementation. Represented as a Clojure (Java) `BigDecimal`.

- `::date-time`

  A timestamp in the form of date and time; equivalent to `DateTime` in the
  reference implementation. Represented as a clj-time `date-time` value.

- `::list`

  A list of AMP values of a particular type; equivalent to `ListOf` in the
  reference implementation. Represented as a Clojure vector. The element type
  is specified by a `:of` item in the argument map; for example, `{:type
  ::a/list :of ::a/integer}` for a list of integer values.


### Servers and clients

TODO

### Defining new argument types

TODO

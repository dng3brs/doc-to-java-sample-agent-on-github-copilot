---
description: "Use when generating Java source code from design documents. Defines coding conventions for generated Java classes including license headers, Javadoc style, error handling patterns, and annotation usage. Applies when creating or editing Java service implementation classes derived from design specs."
applyTo: "**/*.java"
---

# Java Code Generation Conventions

## License Header
All generated Java files must include the project's Apache 4.0 license header:
```java
/*
 * Copyright(c) 2026 Dummy Corporation.
 *
 * Modified by Dummy Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
```

## Class Structure

1. **Package declaration** — Must match the design document specification.
2. **Import statements** — List exactly from the design document. Group by:
   - `java.*` and `javax.*` / `jakarta.*` (standard Java)
   - `org.springframework.*` (framework)
   - `org.terasoluna.gfw.*` (Terasoluna framework)
   - `jp.co.ntt.atrs.*` (project-specific)
3. **Class Javadoc** — Brief description of class purpose. `@author` is optional.
4. **Class annotations** — In the order specified in the class signature.
5. **Member variables** — Group injection annotations (`@Value`, `@Inject`) with package-private visibility. No `private` modifier for injected fields.

## Method Implementation Patterns

### Public Override Methods
```java
@Override
public ReturnType methodName(ParamType param) {
    // 1. Parameter validation
    // 2. Business logic
    // 3. Return result
}
```

### Private Helper Methods
```java
private void helperMethod(ParamType param) throws AtrsBusinessException {
    // validation and business logic
}
```

### Validation Pattern
```java
if (param == null) {
    throw new IllegalArgumentException("param must not null.");
}
```

### Business Exception Pattern
```java
throw new AtrsBusinessException(TicketReserveErrorCode.E_AR_B2_2004, paramValue);
```

### System Exception Pattern (unexpected DB state)
```java
throw new SystemException(LogMessages.E_AR_A0_L9002.getCode(), actualValue, expectedValue);
```

## Coding Rules

- Use `org.springframework.util.StringUtils.hasText()` for checking non-blank strings (not null, not empty, not whitespace).
- Use `org.springframework.util.StringUtils.hasLength()` for checking non-empty strings (not null, not empty).
- Use `jakarta.inject.Inject` (not `javax.inject.Inject` or `org.springframework.beans.factory.annotation.Autowired`) for dependency injection.
- For private methods receiving list parameters that could be null, add an early return guard: `if (param == null) { return; }`.
- Method implementations must match the design document's processing steps exactly, including the order of operations.
- When a design document specifies numbered steps, the implementation should follow the same numbered structure in comments.

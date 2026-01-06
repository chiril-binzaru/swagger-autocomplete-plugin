# Swagger Autocomplete for SQL

An IntelliJ IDEA plugin that provides intelligent autocompletion for SQL aliases based on Swagger/OpenAPI schema definitions.

## Overview

When working with SQL queries that map to API response models, maintaining consistency between database column aliases and API field names can be tedious and error-prone. This plugin bridges that gap by providing:

- **Autocomplete suggestions** for SQL aliases based on Swagger model properties
- **Real-time validation** of alias paths against the schema
- **Code generation** for SELECT statements with all model fields
- **Utility actions** for managing quotes and copying SQL

## Features

### Autocomplete

Type `AS ` after a column and get suggestions from your Swagger model:

```sql
-- @swagger: path/to/swagger.json
-- @model: CustomerProfile

SELECT
    t.id AS customerId,          -- Autocomplete suggests model fields
    t.name AS billingAddress.    -- Nested models supported via dot notation
```

### Validation

- Valid aliases are highlighted in green
- Invalid field names show red error highlighting with descriptive messages
- Supports nested paths like `address.street.zipCode`

### Keyboard Shortcuts

| Shortcut | Action | Description |
|----------|--------|-------------|
| `Ctrl+Alt+G` | Generate SELECT | Generates a complete SELECT statement with all fields from the model, recursively expanding nested objects |
| `Ctrl+Alt+Q` | Toggle Quotes | Adds or removes double quotes from all aliases in the file |
| `Ctrl+Alt+C` | Copy Clean | Copies SQL to clipboard without `@swagger` and `@model` annotation lines |

## Usage

### 1. Add Annotations

Add Swagger annotations at the top of your SQL file:

```sql
-- @swagger: D:\path\to\your\swagger.json
-- @model: YourModelName
```

The `@swagger` path can be absolute or relative to the SQL file location.

### 2. Write Your Query

Start writing your SELECT statement and use `AS` to trigger autocompletion:

```sql
SELECT
    t.column1 AS firstName,
    t.column2 AS lastName,
    t.column3 AS billingAddress.streetName
FROM your_table t
```

### 3. Generate Full SELECT (Optional)

Press `Ctrl+Alt+G` to automatically generate a SELECT statement with all fields from the model:

```sql
SELECT
     AS customerId,
     AS firstName,
     AS lastName,
     AS billingAddress.streetName,
     AS billingAddress.city,
     AS billingAddress.zipCode
FROM
```

## Supported Schema Formats

- OpenAPI 3.0 (`components/schemas`)
- Swagger 2.0 (`definitions`)

## Requirements

- IntelliJ IDEA 2024.3 or later
- Java 21+

## Installation

### From Source

1. Clone the repository
2. Run `./gradlew build`
3. Install the plugin from `build/distributions/swagger-autocomplete-plugin-*.zip`

### Development

```bash
# Build the plugin
./gradlew build

# Run IntelliJ IDEA with the plugin installed (sandbox)
./gradlew runIde

# Run tests
./gradlew test
```

## Example

Given a Swagger schema:

```json
{
  "components": {
    "schemas": {
      "CustomerProfile": {
        "type": "object",
        "properties": {
          "customerId": { "type": "integer" },
          "firstName": { "type": "string" },
          "billingAddress": { "$ref": "#/components/schemas/Address" }
        }
      },
      "Address": {
        "type": "object",
        "properties": {
          "street": { "type": "string" },
          "city": { "type": "string" }
        }
      }
    }
  }
}
```

The plugin will provide autocompletion for:
- `customerId`
- `firstName`
- `billingAddress.street`
- `billingAddress.city`

## License

MIT

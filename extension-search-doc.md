# Extension Search Field

The search field supports both simple keyword searches and advanced filtering.

## 1. Simple Search
Type any word to search in:
- **Name**
- **Short name**
- **Keywords**
- **Category**
- **Artifact ID**

**Example:**
```
hibernate
```
Matches any extension whose name, short name, keywords, category, or artifact ID contains `hibernate`.

## 2. Field Search (`field:expression`)
Search in a specific field using the `field:value` format. If a field has aliases, you can use any of them.

### Available fields and aliases
| Field        | Aliases              | Example                       |
|--------------|----------------------|--------------------------------|
| `name`       | —                    | `name:hibernate`              |
| `description`| `desc`               | `desc:cache`                   |
| `group-id`   | `groupid`, `group`   | `group:io.quarkus`             |
| `artifact-id`| `artifactid`, `artifact` | `artifact:hibernate-orm`   |
| `short-name` | `shortname`          | `shortname:jaxrs`              |
| `keywords`   | `keyword`            | `keyword:database`             |
| `tags`       | `tag`                | `tag:stable`                   |
| `category`   | `cat`                | `category:cloud`               |

Multiple values can be separated by commas:
```
keyword:database,sql
```
Quotes can be used for multi-word expressions:
```
desc:"reactive sql"
```

## 3. "In" Search (`expression in field1,field2`)
Search for words in specific fields.

**Example:**
```
sql in name,description
```
Finds extensions where `sql` appears in **name** or **description**.

Multiple words must all appear in the specified fields:
```
reactive sql in description
```

## 4. Origin Filter
Filter by extension origin:
- `origin:platform` → Only platform extensions
- `origin:other` → Only non-platform extensions

**Example:**
```
hibernate origin:platform
```

## 5. Tags and Categories
Tags and categories can be searched like normal fields.

**Example:**
```
category:messaging
tag:stable
```

## 6. Combining Filters
You can combine different filter types in one query.

**Example:**
```
reactive in name,description category:messaging origin:platform
```
- Looks for "reactive" in name or description
- Filters to the `messaging` category
- Shows only platform extensions

## 7. Short Name Match Priority
If your query exactly matches a short name, that extension appears at the top of results.

**Example:**
```
jaxrs
```

## 8. Empty Search
If the field is empty, all extensions are shown.

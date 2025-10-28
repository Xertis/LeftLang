| Тип данных в Left со знаком | вариант без знака | Аналог в C99 | Подключает stdint.h? |
| --------------------------- | ----------------- | ------------ | -------------------- |
| char                        | Char              | char         | Нет                  |
| short                       | Short             | short        | Нет                  |
| int                         | Int               | int          | Нет                  |
| long                        | Long              | long         | Нет                  |
| heavy                       | Heavy             | long long    | Нет                  |
| i8                          | u8                | int8_t       | Да                   |
| i16                         | u16               | int16_t      | Да                   |
| i32                         | u32               | int32_t      | Да                   |
| i64                         | u64               | int64_t      | Да                   |
| imax                        | umax              | intmax_t     | Да                   |
| FastI8                      | FastU8            | int_fast8_t  | Да                   |
| FastI16                     | FastU16           | int_fast16_t | Да                   |
| FastI32                     | FastU32           | int_fast32_t | Да                   |
| FastI64                     | FastU64           | int_fast64_t | Да                   |
| f32                         | -                 | float        | Нет                  |
| f64                         | -                 | double       | Нет                  |
| Bool                        | -                 | _Bool        | Нет                  |
| Void                        | -                 | void         | Нет                  |

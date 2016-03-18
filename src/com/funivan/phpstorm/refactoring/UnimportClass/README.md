# Unimport class
## Why?
This quick fix is usefull when you want to copy your code to other source (ex: send to your friend, copy to examples). 
And it is very annoying to copy/move use statements.

## How to use
1. Select target class
2. Run intention


## I/O
We can convert this code:
```php
use My\Library\UserModel;
$date = new UserModel();
```
To this:
```php
use My\Library\UserModel;
$date = new \My\Library\UserModel();
```
This fix does not remove unused use. You can do this manually or on code reformat.

# Find magic methods

## Why?
There are two types of magick methods. Static and non-static.
Whis this cool plugin you can find all magick methods of the target class.

## How to use
1. Navigate to target class
2. Navigate to one of the method: `__call` or `__callStatic` inside your class
3. Invoke action `Find magic methods`


##  Demo:
Imagine we have following code
```php
class Namer {
  public function __call($name, $arg){
    return __CLASS__ . ' ' . $name; 
  }
}


$namer = new Namer();

# magic method call
$namer->user(); 
$namer->test();

```
Using action `Find magick method` you will find methods `user`  and `test`


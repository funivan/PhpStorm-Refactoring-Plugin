# Find magic methods

## Why?
There are two types of magic methods: static and non-static.
You can find all magic methods of a target class with this cool plugin.

## How to use
1. Navigate to a target class.
2. Navigate to one of the methods: `__call`, `__callStatic`, `__get`, `__set` inside your class.
3. Invoke the action `Find magic method call`.


##  Demo:
Imagine that we have the following code:
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
Using the action `Find magic method`, you will find `user`  and `test` methods.


package com.funivan.phpstorm.refactoring.findMagicMethods

import com.funivan.phpstorm.refactoring.findMagicMethods.results.ResultCollectorInterface

/**
 * @author Ivan Shcherbak alotofall@gmail.com>
 */
interface MagicElementVisitor {
    var collector: ResultCollectorInterface
}

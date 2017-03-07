package utilities

import slick.lifted.{Rep, Query, CanBeQueryCondition}

/**
 * Optionally filter on a column with a supplied predicate
 * @param query The initial query on which the filters must be applied
 */
case class MaybeFilter[E, U, C[_]](query: Query[E, U, C]) {

  /**
   * Filter with predicate `pred` only if `data` is defined
   */
  def filter[X, T <: Rep[_]](data: Option[X])(pred: X => E => T)(implicit wr: CanBeQueryCondition[T]): MaybeFilter[E, U, C] = {
    data match {
      case Some(value) => MaybeFilter(query.filter(pred(value)))
      case None => this
    }
  }

}

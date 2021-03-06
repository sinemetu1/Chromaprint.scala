package chromaprint

import fs2.Pipe

object IntegralImage {

  def pipe[F[_]]: Pipe[F,IndexedSeq[Double],IndexedSeq[Double]] =
    _.mapAccumulate[Option[IndexedSeq[Double]],IndexedSeq[Double]](None){
      (o, thisRow) => (o match {
        case None =>
          require(thisRow.nonEmpty)
          thisRow.indices.drop(1).foldLeft(thisRow){
            (r, i) => r.updated(i, r(i) + r(i - 1))
          }
        case Some(prevRow) =>
          require(thisRow.length == prevRow.length)
          thisRow.indices.drop(1).foldLeft(thisRow.updated(0, thisRow(0) + prevRow(0))){
            (r, i) => r.updated(i, r(i) + r(i - 1) + prevRow(i) - prevRow(i - 1))
          }
      }) match {
        case nextRow =>
          (Some(nextRow), nextRow)
      }
    }.map(_._2)

  def area(integral: IndexedSeq[IndexedSeq[Double]], x1: Int, y1: Int, x2: Int, y2: Int): Double =
    if (x2 < x1 || y2 < y1) {
      0D
    } else {
      integral(x2)(y2) +
        (if (x1 > 0) {
          - integral(x1 - 1)(y2) +
            (if (y1 > 0) {
              integral(x1 - 1)(y1 - 1)
            } else {
              0
            })
        } else {
          0
        }) +
        (if (y1 > 0) {
          - integral(x2)(y1 - 1)
        } else {
          0
        })
    }

}

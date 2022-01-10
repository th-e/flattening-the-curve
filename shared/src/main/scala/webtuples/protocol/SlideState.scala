package webtuples.protocol

case class SlideState(slideIndex: Int) {

  def prevSlide: SlideState = copy(slideIndex = (slideIndex - 1) max 0)
  def nextSlide: SlideState = copy(slideIndex = slideIndex + 1)
}

object SlideState {
  def empty: SlideState = SlideState(0)
}

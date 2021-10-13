package com.quran.labs.androidquran.presenter.quran.ayahtracker

import com.quran.data.core.QuranInfo
import com.quran.data.model.SuraAyah
import com.quran.data.model.selection.SelectionIndicator
import com.quran.labs.androidquran.data.QuranDisplayData
import com.quran.labs.androidquran.ui.helpers.HighlightType
import com.quran.labs.androidquran.ui.helpers.QuranDisplayHelper
import com.quran.labs.androidquran.ui.util.ImageAyahUtils
import com.quran.labs.androidquran.util.QuranUtils
import com.quran.labs.androidquran.view.HighlightingImageView
import com.quran.page.common.data.AyahBounds
import com.quran.page.common.data.AyahCoordinates
import com.quran.page.common.data.PageCoordinates
import com.quran.page.common.draw.ImageDrawHelper

open class AyahImageTrackerItem @JvmOverloads constructor(
  page: Int,
  private val quranInfo: QuranInfo?,
  private val quranDisplayData: QuranDisplayData,
  private val isPageOnRightSide: Boolean = false,
  private val imageDrawHelpers: Set<ImageDrawHelper>,
  val ayahView: HighlightingImageView
) : AyahTrackerItem(page) {
  @JvmField var coordinates: Map<String, List<AyahBounds>>? = null

  override fun onSetPageBounds(pageCoordinates: PageCoordinates) {
    if (this.page == pageCoordinates.page) {
      // this is only called if overlayText is set
      val pageBounds = pageCoordinates.pageBounds
      if (!pageBounds.isEmpty) {
        ayahView.setPageBounds(pageBounds)
        val context = ayahView.context
        val suraText = quranDisplayData.getSuraNameFromPage(context, page, true)
        val juzText = quranDisplayData.getJuzDisplayStringForPage(context, page)
        val pageText = QuranUtils.getLocalizedNumber(context, page)
        val rub3Text = QuranDisplayHelper.displayRub3(context, quranInfo, page)
        ayahView.setOverlayText(context, suraText, juzText, pageText, rub3Text)
      }
      ayahView.setPageData(pageCoordinates, imageDrawHelpers)
    }
  }

  override fun onSetAyahCoordinates(ayahCoordinates: AyahCoordinates) {
    if (this.page == ayahCoordinates.page) {
      val coordinates = ayahCoordinates.ayahCoordinates
      this.coordinates = coordinates
      if (coordinates.isNotEmpty()) {
        ayahView.setAyahData(ayahCoordinates)
        ayahView.invalidate()
      }
    }
  }

  override fun onHighlightAyah(
    page: Int,
    sura: Int,
    ayah: Int,
    type: HighlightType,
    scrollToAyah: Boolean
  ): Boolean {
    if (this.page == page && coordinates != null) {
      ayahView.highlightAyah(sura, ayah, type)
      ayahView.invalidate()
      return true
    } else if (coordinates != null) {
      ayahView.unHighlight(type)
    }
    return false
  }

  override fun onHighlightAyat(page: Int, ayahKeys: Set<String>, type: HighlightType) {
    if (this.page == page) {
      ayahView.highlightAyat(ayahKeys, type)
      ayahView.invalidate()
    }
  }

  override fun onUnHighlightAyah(page: Int, sura: Int, ayah: Int, type: HighlightType) {
    if (this.page == page) {
      ayahView.unHighlight(sura, ayah, type)
    }
  }

  override fun onUnHighlightAyahType(type: HighlightType) {
    ayahView.unHighlight(type)
  }

  override fun getToolBarPosition(page: Int, sura: Int, ayah: Int): SelectionIndicator {
    if (this.page == page) {
      val coordinates = coordinates
      val bounds = if (coordinates == null) null else coordinates["$sura:$ayah"]
      val screenWidth = ayahView.width
      if (bounds != null && screenWidth > 0) {
        val yPadding = ayahView.paddingTop
        val xPadding = if (isPageOnRightSide) ayahView.width else 0
        return ImageAyahUtils.getToolBarPosition(bounds, ayahView.imageMatrix, xPadding, yPadding)
      }
    }
    return SelectionIndicator.None
  }

  override fun getAyahForPosition(page: Int, x: Float, y: Float): SuraAyah? {
    return if (page == page) ImageAyahUtils.getAyahFromCoordinates(
      coordinates,
      ayahView,
      x,
      y
    ) else null
  }
}

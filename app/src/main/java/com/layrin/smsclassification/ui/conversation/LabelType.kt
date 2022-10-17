package com.layrin.smsclassification.ui.conversation

sealed class LabelType {
    object Normal: LabelType() {
        override fun toString(): String {
            return "Normal"
        }
    }
    object Fraud: LabelType() {
        override fun toString(): String {
            return "Scam"
        }
    }
    object StatusAds: LabelType() {
        override fun toString(): String {
            return "Ads/Status"
        }
    }
}

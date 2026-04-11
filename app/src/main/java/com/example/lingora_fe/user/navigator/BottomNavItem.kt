package com.example.lingora_fe.user.navigator

sealed class BottomNavItem(
    val label: String,
    val icon: Int, // Drawable resource ID for unselected icon
    val selectedIcon: Int // Drawable resource ID for selected icon
) {
    object Vocabulary : BottomNavItem(
        label = "Từ vựng",
        icon = com.example.lingora_fe.R.drawable.ic_vocab,
        selectedIcon = com.example.lingora_fe.R.drawable.ic_vocab_selected
    )
    object Practice : BottomNavItem(
        label = "Luyện tập",
        icon = com.example.lingora_fe.R.drawable.ic_practice,
        selectedIcon = com.example.lingora_fe.R.drawable.ic_practice_selected
    )
    object StudySet : BottomNavItem(
        label = "Học liệu",
        icon = com.example.lingora_fe.R.drawable.ic_material,
        selectedIcon = com.example.lingora_fe.R.drawable.ic_material_selected
    )
    object Dictionary : BottomNavItem(
        label = "Từ điển",
        icon = com.example.lingora_fe.R.drawable.ic_dictionary,
        selectedIcon = com.example.lingora_fe.R.drawable.ic_dictionary_selected
    )
    object Forum : BottomNavItem(
        label = "Diễn đàn",
        icon = com.example.lingora_fe.R.drawable.ic_forum,
        selectedIcon = com.example.lingora_fe.R.drawable.ic_forum_selected
    )
    object Classroom : BottomNavItem(
        label = "Lớp học",
        icon = com.example.lingora_fe.R.drawable.ic_material, // using material as placeholder
        selectedIcon = com.example.lingora_fe.R.drawable.ic_material_selected
    )
    object Profile : BottomNavItem(
        label = "Cá nhân",
        icon = com.example.lingora_fe.R.drawable.ic_profile,
        selectedIcon = com.example.lingora_fe.R.drawable.ic_profile_selected
    )
}


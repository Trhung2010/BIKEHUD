package com.example.data

import com.example.model.NavStep
import com.example.model.RoutePreset
import com.example.model.TurnType

object NavigationRepository {

    val routes: List<RoutePreset> = listOf(
        RoutePreset(
            id = "saigon_landmark",
            name = "Bến Thành ➔ Landmark 81",
            destination = "Landmark 81, Bình Thạnh",
            totalDistanceKm = 6.8f,
            estimatedTimeMinutes = 18,
            steps = listOf(
                NavStep("Đi thẳng trên đường Lê Lợi", "Đường Lê Lợi", 350, TurnType.STRAIGHT, "Làn giữa xe máy"),
                NavStep("Rẽ trái vào đường Đồng Khởi", "Đường Đồng Khởi", 500, TurnType.TURN_LEFT, "Chú ý đèn tín hiệu"),
                NavStep("Rẽ phải qua Cầu Ba Son", "Cầu Ba Son (Thủ Thiêm 2)", 1200, TurnType.TURN_RIGHT, "Làn xe máy trên cầu"),
                NavStep("Nhập làn vào Đại lộ Mai Chí Thọ", "Đại lộ Mai Chí Thọ", 1800, TurnType.SLIGHT_RIGHT, "Tốc độ tối đa 60 km/h"),
                NavStep("Rẽ trái vào đường Nguyễn Cơ Thạch", "Đường Nguyễn Cơ Thạch", 900, TurnType.TURN_LEFT, "Làn rẽ trái"),
                NavStep("Rẽ phải qua Cầu Thủ Thiêm 1", "Cầu Thủ Thiêm", 1400, TurnType.TURN_RIGHT, "Qua hướng Bình Thạnh"),
                NavStep("Vào đường Điện Biên Phủ", "Đường Điện Biên Phủ", 650, TurnType.SLIGHT_RIGHT, "Làn gom xe hai bánh"),
                NavStep("Đến đích: Landmark 81 bên phải", "Khu Vinhomes Central Park", 0, TurnType.DESTINATION, "Điểm đến an toàn")
            )
        ),
        RoutePreset(
            id = "hanoi_noibai",
            name = "Hồ Gươm ➔ Cầu Nhật Tân",
            destination = "Cầu Nhật Tân, Tây Hồ, Hà Nội",
            totalDistanceKm = 12.5f,
            estimatedTimeMinutes = 26,
            steps = listOf(
                NavStep("Đi thẳng theo Phố Đinh Tiên Hoàng", "Phố Đinh Tiên Hoàng", 400, TurnType.STRAIGHT, "Tốc độ 40 km/h"),
                NavStep("Rẽ phải vào Phố Hàng Vôi", "Phố Hàng Vôi", 600, TurnType.TURN_RIGHT, "Phố cổ chú ý người đi bộ"),
                NavStep("Đi chếch sang trái vào Đường Yên Phụ", "Đường Yên Phụ", 2100, TurnType.SLIGHT_LEFT, "Làn đường ven sông"),
                NavStep("Đi tiếp vào Đường Nghi Tàm", "Đường Nghi Tàm - Âu Cơ", 3200, TurnType.STRAIGHT, "Tốc độ 50 km/h"),
                NavStep("Rẽ trái vào Đường Võ Chí Công", "Đường Võ Chí Công", 1800, TurnType.TURN_LEFT, "Đường rộng 8 làn"),
                NavStep("Lên Cầu Nhật Tân", "Cầu Nhật Tân", 4400, TurnType.STRAIGHT, "Làn xe máy bên phải"),
                NavStep("Đến điểm ngắm cảnh Cầu Nhật Tân", "Đầu cầu phía Bắc", 0, TurnType.DESTINATION, "Đã tới đích")
            )
        ),
        RoutePreset(
            id = "danang_haivan",
            name = "Đà Nẵng ➔ Đỉnh Đèo Hải Vân",
            destination = "Hải Vân Quan (Đỉnh đèo)",
            totalDistanceKm = 24.0f,
            estimatedTimeMinutes = 45,
            steps = listOf(
                NavStep("Đi thẳng trên Đường Nguyễn Tất Thành", "Đường Nguyễn Tất Thành", 4200, TurnType.STRAIGHT, "Cung đường biển"),
                NavStep("Nhập vào Quốc Lộ 1A hướng Đèo", "Quốc Lộ 1A", 3500, TurnType.SLIGHT_LEFT, "Tránh xe tải lớn"),
                NavStep("Bắt đầu lên dốc Đèo Hải Vân", "Đường Đèo Hải Vân", 2800, TurnType.TURN_RIGHT, "Cẩn thận sương mù"),
                NavStep("Cua tay áo nguy hiểm bên phải", "Cua Chữ U Đèo", 850, TurnType.SHARP_RIGHT, "Về số 2 - Giảm tốc"),
                NavStep("Cua gắt sang trái vách núi", "Đoạn Đèo View Biển", 1200, TurnType.SHARP_LEFT, "Bấm còi cảnh báo"),
                NavStep("Đi thẳng lên đỉnh đèo", "Đoạn dốc Hải Vân", 2500, TurnType.STRAIGHT, "Gió lớn trên cao"),
                NavStep("Đến Hải Vân Quan - Đỉnh Đèo", "Hải Vân Quan", 0, TurnType.DESTINATION, "Hoàn thành cung đèo")
            )
        ),
        RoutePreset(
            id = "dalat_prenn",
            name = "Hồ Xuân Hương ➔ Đèo Prenn",
            destination = "Chân Đèo Prenn, Đà Lạt",
            totalDistanceKm = 14.2f,
            estimatedTimeMinutes = 28,
            steps = listOf(
                NavStep("Đi vòng qua Vòng xuyến Hồ Xuân Hương", "Vòng xuyến Thủy Tạ", 400, TurnType.ROUNDABOUT, "Lối ra thứ 2"),
                NavStep("Vào Đường Ba Tháng Tư", "Đường 3 Tháng 4", 2300, TurnType.STRAIGHT, "Đường dốc thông reo"),
                NavStep("Bắt đầu đổ Đèo Prenn mới mở rộng", "Đèo Prenn 4 làn xe", 3100, TurnType.SLIGHT_RIGHT, "Mặt đường êm ái"),
                NavStep("Khúc cua thoai thoải bên trái", "Cua Thác Datanla", 1800, TurnType.SLIGHT_LEFT, "Tốc độ khuyến nghị 45 km/h"),
                NavStep("Đến Trạm dừng Thác Datanla", "Khu du lịch Datanla", 0, TurnType.DESTINATION, "Đã tới điểm dừng chân")
            )
        )
    )
}

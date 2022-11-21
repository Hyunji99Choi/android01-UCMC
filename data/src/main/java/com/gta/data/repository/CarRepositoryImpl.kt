package com.gta.data.repository

import com.gta.data.model.UserInfo
import com.gta.data.source.CarDataSource
import com.gta.data.source.UserDataSource
import com.gta.domain.model.Car
import com.gta.domain.model.CarDetail
import com.gta.domain.model.CarState
import com.gta.domain.model.SimpleCar
import com.gta.domain.model.UserProfile
import com.gta.domain.model.toSimple
import com.gta.domain.repository.CarRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class CarRepositoryImpl @Inject constructor(
    private val userDataSource: UserDataSource,
    private val carDataSource: CarDataSource
) : CarRepository {
    override fun getOwnerId(carId: String): String {
        return "(test)OwnerId"
    }

    override fun getNowRentUser(carId: String): String? {
        return "(test)RentedId"
    }

    override fun getCarData(carId: String): Flow<CarDetail> {
        return MutableStateFlow(
            CarDetail(
                carId,
                "새 차 몰고 싶을 때",
                CarState.AVAILABLE,
                "신당동 앞마당",
                "아반테 신형",
                183000,
                "깨끗이 써주세요. 찾아 갑니다.",
                emptyList(),
                UserProfile("(test)OwnerId", "(test)선구자", 25F, null)
            )
        )
    }

    override fun getSimpleCarList(ownerId: String): Flow<List<SimpleCar>> = callbackFlow {
        userDataSource.getUser(ownerId).addOnSuccessListener { user ->
            if (user.exists()) {
                val cars = mutableListOf<SimpleCar>()
                user.toObject(UserInfo::class.java)?.myCars?.forEach { carId ->
                    carDataSource.getCar(carId).addOnSuccessListener { car ->
                        if (user.exists()) {
                            cars.add(
                                car.toObject(Car::class.java)?.toSimple(car.id) ?: SimpleCar()
                            )
                            trySend(cars)
                        }
                    }
                }
            }
        }
        awaitClose()
    }
}

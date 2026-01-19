package com.indri.vsmentproject.ui.viewmodel
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
//import com.indri.vsmentproject.data.model.notification.NotifikasiModel
//import com.indri.vsmentproject.data.model.task.TugasModel
//import com.indri.vsmentproject.data.model.user.UserModel
//import com.indri.vsmentproject.data.model.villa.VillaModel
//import com.indri.vsmentproject.data.repository.*
//import com.indri.vsmentproject.data.utils.Resource
//
class MainViewModel : ViewModel() {
//
//    // Inisialisasi Repository
//    private val authRepo = AuthRepository()
//    private val villaRepo = VillaRepository()
//    private val staffRepo = StaffRepository()
//    private val taskRepo = TaskRepository()
//    private val notifRepo = NotificationRepository()
//
//    // --- AUTH LOGIC ---
//    private val _loginStatus = MutableLiveData<Resource<String>>()
//    val loginStatus: LiveData<Resource<String>> = _loginStatus
//
//    fun loginUser(email: String, pass: String) {
//        authRepo.login(email, pass) { resource ->
//            _loginStatus.postValue(resource)
//        }
//    }
//
//    // Perbaikan Logout: Pastikan AuthRepository punya fungsi logout()
//    fun logout() {
//        authRepo.logout()
//    }
//
//    // --- DATA LOGIC ---
//    fun getVillas(managerUid: String): LiveData<Resource<List<VillaModel>>> {
//        return villaRepo.getVillasByManager(managerUid)
//    }
//
//    fun getStaffs(managerUid: String): LiveData<Resource<List<UserModel>>> {
//        return staffRepo.getStaffByManager(managerUid)
//    }
//
//    fun getTasks(villaId: String): LiveData<Resource<List<TugasModel>>> {
//        return taskRepo.getTasksByVilla(villaId)
//    }
//
//    fun getNotifications(myUid: String): LiveData<Resource<List<NotifikasiModel>>> {
//        return notifRepo.getMyNotifications(myUid)
//    }
}
import {Component, OnInit} from '@angular/core';
import {
  IonicPage,
  Keyboard,
  ModalController,
  NavController,
  NavParams,
  ToastController,
  App,
  LoadingController
} from 'ionic-angular';
import {LoginPage} from "../login/login";
import {DatePickerProvider} from "ionic2-date-picker";
import {AngularFireAuth} from "angularfire2/auth";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {CategoryPage, Newcategory} from "../category/category";
import {HomePage} from "../home/home";

export interface Item {
  entry_time: String;
  category: String;
  note: String;
  amount: String;
  type: String;
  created_by: any;
}

/**
 * Generated class for the ItemPage page.
 *
 * See https://ionicframework.com/docs/components/#navigation for more info on
 * Ionic pages and navigation.
 */

@IonicPage()
@Component({
  selector: 'page-item',
  templateUrl: 'item.html',
})
export class ItemPage implements OnInit{

  public icons: any = [
    'happy',
    'heart',
    'bulb',
    'cafe',
    'camera',
    'car',
    'home',
    'image',
    'cash',
    'man',
    'logo-usd',
    'person',
    'pricetag',
    'print',
    'restaurant',
    'rose',
    'sad',
    'star',
  ]
  public categoryTypes = [
    {name:'Salary', text:'salary', icon:'card'},
    {name:'Part-time', text:'part-time', icon:'logo-usd'},
    {name:'Financial', text:'financial', icon:'logo-bitcoin'},
    {name:'Other', text:'other', icon:'globe'},

    //{name:'Other', text:'other', icon:'globe'},

  ]
  item: Item = {
    entry_time: (new Date().toLocaleDateString()).toString(),
    category: '',
    note: '',
    amount: '',
    type: '',
    created_by:'',
  };

  newcategory: Newcategory ={
    name: '',
    icon: '',
    type: '',
  };

  isNewCategory: Boolean = false;

  constructor(public navCtrl: NavController,
              public navParams: NavParams,
              private datePickerProvider: DatePickerProvider,
              //public cate: CategoryProvider,
              //private trans: TransactionProvider,
              public modalCtrl: ModalController,
              private keyboard: Keyboard,
              private toastCtrl: ToastController,
              private afAuth: AngularFireAuth,
              public  http:HttpClient,
              public app:App,
              public loadingCtrl:LoadingController) {
  }

  // ionViewDidLoad() {
  //   console.log('ionViewDidLoad ItemPage');
  // }
  // chooseCategory() {
  //     this.navCtrl.push('CategoryPage', {item : this.item})
  //
  // }

  newCategory() {
    this.isNewCategory = true;
  }

  onSelectedIcon(icon: String) {
    this.newcategory.icon = icon;
    console.log("newcategory ");
  }
  presentToast(message: string) {
    let toast = this.toastCtrl.create({
      message: message,
      duration: 3000,
      position: 'bottom'
    });
    toast.present();
    return toast;
  }
  back(){
    this.navCtrl.push('ItemPage')
  }
  addCategory(newcategory: Newcategory) {
    if (newcategory.name === '') {
      let toast = this.toastCtrl.create({
        message: 'Category name must be filled',
        duration: 3000,
        position: 'bottom'
      });
      toast.present();
    } else if (newcategory.icon === '') {
      let toast = this.toastCtrl.create({
        message: 'Category icon should be selected',
        duration: 3000,
        position: 'bottom'
      });
      toast.present();
    } else {
      // set type catergory
      this.newcategory.type = 'income';
      console.log("add newcategory ");
      var jsonData = {
        type: this.newcategory.type,  //type can be cost or income
        name: this.newcategory.name,  //category is any subcategory under type
        date: this.item.entry_time,
        //money:this.item.amount,
        //comment:this.item.note,
        icon: this.newcategory.icon,
      }

      let myheaders = new HttpHeaders({ });
      this.http.post("http://localhost:3000/addCategory", jsonData, {headers: myheaders, withCredentials: true , responseType:'text'})
        .subscribe((data) => {
          if (data == "SUCCESS") {
            console.log("data was added");
            //stuff
            this.presentToast("Category was added");

          } else if (data == "NOT_LOGGED_IN") {
            console.log("user not logged in");
            //stuff


          }
          else if (data == "NO_TYPE") {
            console.log("no type (income or cost) was entered");
            //stuff

          } else if (data == "NO_NAME") {
            console.log("no date present");
            //stuff

          }else if (data == "NO_CATEGORY") {
            console.log("no category was add");
            //stuff


          } else {
            console.log(data);
            //stuff

          }
        }, error => {
          console.log("Error connecting to backend server");
        });
      setTimeout(()=>{
        this.navCtrl.push('ItemPage');
      },100);
    }
  }

  click(category: String) {
    this.item.category = category;
    //this.item.type = "expense";
  }
  showCalendar() {
    const dateSelected =
      this.datePickerProvider.showCalendar(this.modalCtrl);

    dateSelected.subscribe(date =>
      this.item.entry_time = (date.toLocaleDateString()).toString()
    );
  }
  handleEnter() {
    this.keyboard.close();
  }

  Save(type:String) {
    if (this.item.amount.length <= 0) {
      this.presentToast("Please input your expense");
    }
    else if (this.item.category.length <= 0) {
      this.presentToast("Please choose one category");
    }
    else {
      this.item.type = type;

      var jsonData = {
        'type': this.item.type,  //type can be cost or income
        'category': this.item.category,  //category is any subcategory under type
        'date':this.item.entry_time,
        'money':this.item.amount,
        'comment':this.item.note
      };

      let myheaders = new HttpHeaders({ });
      this.http.post("http://localhost:3000/addRecord", jsonData, {headers: myheaders, withCredentials: true , responseType:'text'})
        .subscribe((data) => {
          if (data == "SUCCESS") {
            console.log("data was added");
            //stuff


          } else if (data == "NOT_LOGGED_IN") {
            console.log("user not logged in");
            //stuff


          } else if (data == "NO_CATEGORY") {
            console.log("no category was entered");
            //stuff


          } else if (data == "NO_TYPE") {
            console.log("no type (income or cost) was entered");
            //stuff

          } else if (data == "NO_DATE") {
            console.log("no date present");
            //stuff

          } else {
            console.log(data);
            //stuff

          }
        }, error => {
          console.log("Error connecting to backend server");
        });
      this.navCtrl.push('ItemPage');
    }

    console.log(jsonData)
    let loading = this.loadingCtrl.create({
      spinner: 'hide',
      content: 'Loading Please Wait...'
    });

    loading.present();

    setTimeout(() => {
      this.navCtrl.push(HomePage);
      this.navCtrl.push('ItemPage');
    }, 100);

    setTimeout(() => {
      loading.dismiss();
    }, 1000);
  }
  presentNewExpense() {

    // let profileModal = this.modalCtrl.create('ItemPage', { type: type });
    // profileModal.present();
    this.navCtrl.push(CategoryPage)

  }


  test=[];
  ngOnInit() {
    let myheader = new HttpHeaders();
    let uri = "http://localhost:3000/addCategory?type=income";
    this.http.get(uri,{headers: myheader, withCredentials: true})
      .subscribe((data: any[]) => {
        this.test = data;
      })
  }
  // presentNewExpense() {
  //
  //   // let profileModal = this.modalCtrl.create('ItemPage', { type: type });
  //   // profileModal.present();
  //   this.navCtrl.push('ItemPage')
  //
  // }
}

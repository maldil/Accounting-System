import {Component, OnInit} from '@angular/core';
import {AlertController, NavController, ActionSheetController, ToastController, App} from 'ionic-angular';
import {LoginPage} from "../login/login";
import {HttpClient, HttpHeaders} from "@angular/common/http";
//import {File} from '@ionic-native/file';


@Component({
  selector: 'page-contact',
  templateUrl: 'contact.html'
})
export class ContactPage implements OnInit{

  headerImage: string; // = './assets/imgs/logo.png';

  constructor(public navCtrl: NavController,
              public editCtrl: AlertController,
              public toastCtrl: ToastController,
              public http: HttpClient,
              public app:App
             ) {

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

  username:string;
  password:string;
  opassword:string;
  npassword:string;
  dob:string;
  firstname: string;
  lastname: string;
  genderlist: string;
  income:string;
  age:string;

  // ionViewDidLoad() {
  //   this.profilelist = this.getProfile();
  // }
  // getPhoto() {
  //   let options = {
  //     maximumImagesCount: 1
  //   };
  //   this.imagePicker.getPictures(options).then((results) => {
  //     for (var i = 0; i < results.length; i++) {
  //       this.imgPreview = results[i];
  //       // this.base64.encodeFile(results[i]).then((base64File: string) => {
  //       //   this.regData.avatar = base64File;
  //       // }, (err) => {
  //       //   console.log(err);
  //       // });
  //     }
  //   }, (err) => { });
  // }




  fileChange(event){
    if(event.target.files && event.target.files[0]){
      let reader = new FileReader();

      reader.onload = (event:any) => {
        this.headerImage = event.target.result;

      }
      reader.readAsDataURL(event.target.files[0]);
    }
    let fileList: FileList = event.target.files;
    let file: File = fileList[0];
    console.log(file);
    let formData = new FormData();
    formData.append('picture', file, file.name);
    let myheaders = new HttpHeaders({'enctype': 'multipart/form-data; boundary=----WebKitFormBoundaryuL67FWkv1CA'});
    this.http.post("http://localhost:3000/addPhoto", formData,{headers: myheaders, withCredentials: true, responseType:'text'})
        .subscribe((data)=>{
		if (data == "SAVED") {
			console.log(data);
        	} else 	{
		      console.log(data);
        	}
        })
  }
  // $('#imagePreview').css('background-image', 'url('+e.target.result +')');
  // $('#imagePreview').hide();
  // $('#imagePreview').fadeIn(650);





  username_edit(){
    let view = this.editCtrl.create({
        title:'Username',
        message:'Alert: username cannot be changed by user!',
        buttons:[
            {
              text:'Ok',
            }
        ]
    });
    view.present();
  }
  fname_edit() {
      document.getElementById("fname").removeAttribute("readonly");

      let edit = this.editCtrl.create({
          title: 'First name',
          message: 'Enter your new first name',
          inputs:[
              {
                  type:'text',
                  id:'first_name',
                  name:'first_name',
                  placeholder:'First name',
                  value:""
              }
          ],
          buttons: [
              {
                  text: 'Cancel',
              },
              {
                  text: 'Save',
                  handler: data => {
                      console.log('Saved clicked');
                      let jsondata = JSON.parse(JSON.stringify(data));
                      // let htmldata = eval('('+jsondata+')');
                      this.firstname = jsondata['first_name'];
                      let myheaders = new HttpHeaders({ });
                      this.http.post("http://localhost:3000/viewprofile", {key: 'FIRST_NAME',value:this.firstname},{withCredentials: true, responseType:'text'})
                          .subscribe((data)=>{
                            if (data == "SAVED") {
                              this.editCtrl.create({
                                  message:"First name has been changed!"
                              });
                            }
                            else{
                              this.editCtrl.create({
                                  message:"Change failed!"
                              });
                            }
                          })
                  }
              }
          ]
      });
      edit.present();
  }
  lname_edit(){
      let edit = this.editCtrl.create({
          title: 'Last name',
          message: 'Enter your new last name',
          inputs:[
              {
                  id:'last_name',
                  name:'last_name',
                  placeholder:'Last name'
              }
          ],
          buttons: [
              {
                  text: 'Cancel',
              },
              {
                  text: 'Save',
                  handler: data => {
                      let jsondata = JSON.parse(JSON.stringify(data));
                      this.lastname = jsondata['last_name'];
                      let myheaders = new HttpHeaders({ });
                      this.http.post("http://localhost:3000/viewprofile", {key: 'LAST_NAME',value:this.lastname},{withCredentials: true, responseType:'text'})
                          .subscribe((data)=>{
                              if (data == "SAVED") {
                                  this.editCtrl.create({
                                      message:"Last name has been changed!"
                                  });
                              }
                              else{
                                  this.editCtrl.create({
                                      message:"Change failed!"
                                  });
                              }
                          })
                      console.log('Saved clicked');
                  }
              }
          ]
      });
      edit.present();
  }
  gender_edit(){
      let edit = this.editCtrl.create()
      edit.setTitle('Gender');
      edit.addInput({
          type:'radio',
          label:'Female',
          value:'Female'
      });
      edit.addInput({
          type:'radio',
          label:'Male',
          value:'Male'
      });
      edit.addInput({
          type:'radio',
          label:'Non-Binary',
          value:'Non-Binary'
      });
      edit.addInput({
          type:'radio',
          label:'Agender',
          value:'Agender'
      });
      edit.addInput({
          type:'radio',
          label:'Bigender',
          value:'Bigender'
      });
      edit.addInput({
          type:'radio',
          label:'Prefer not to say',
          value:'NA'
      });
      edit.addButton('Cancel');
      edit.addButton({
          text: 'Save',
          handler: gender => {
              this.genderlist = gender;
              let myheaders = new HttpHeaders({ });
              this.http.post("http://localhost:3000/viewprofile", {key: 'SEX',value:this.genderlist},{withCredentials: true, responseType:'text'})
                  .subscribe((data)=>{
                      if (data == "SAVED") {
                          this.editCtrl.create({
                              message:"Last name has been changed!"
                          });
                      }
                      else{
                          this.editCtrl.create({
                              message:"Change failed!"
                          });
                      }
                  })
              console.log('Saved clicked');

          }
      });
      edit.present();
  }

  dob_edit(){
      let myheaders =new HttpHeaders({});
      this.http.post("http://localhost:3000/viewprofile",{key:'BIRTH_DAY',value:this.dob},{withCredentials: true, responseType:'text'})
          .subscribe((data)=>{
              if (data == "SAVED") {
                  this.editCtrl.create({
                      message:"Birthday has been changed!"
                  });
              }
              else{
                  this.editCtrl.create({
                      message:"Change failed!"
                  });
              }
          })
      console.log('Saved clicked');
  }
  income_edit() {
      let edit = this.editCtrl.create({
          title: 'Income',
          message: 'Enter your new Income',
          inputs:[
              {
                  id:'income',
                  name:'income',
                  placeholder:'Income'
              }
          ],
          buttons: [
              {
                  text: 'Cancel',
              },
              {
                  text: 'Save',
                  handler: data => {
                      let jsondata = JSON.parse(JSON.stringify(data));
                      this.income = jsondata['income'];
                      let myheaders = new HttpHeaders({ });
                      this.http.post("http://localhost:3000/viewprofile", {key: 'INCOME',value:this.income},{withCredentials: true, responseType:'text'})
                          .subscribe((data)=>{
                              if (data == "SAVED") {
                                  this.editCtrl.create({
                                      message:"Income has been changed!"
                                  });
                              }
                              else{
                                  this.editCtrl.create({
                                      message:"Change failed!"
                                  });
                              }
                          })
                      console.log('Saved clicked');
                  }
              }
          ]
      });
      edit.present();
  }
  password_edit() {
      // document.getElementById("password").removeAttribute("readonly");
      let edit = this.editCtrl.create({
          title: 'Password',
          message: 'Enter your old password and new password',
          inputs:[
              {
                id:'opassword',
                name:'opassword',
                placeholder:'Old password',
                type:'password'
              },
              {
                id:'npassword',
                name:'npassword',
                placeholder:'New password',
                type:'password'
              },
              {
                id:'npassword',
                name:'npassword',
                placeholder:'Confirm password',
                type:'password'
              }
          ],
          buttons: [
              {
                  text: 'Cancel',
              },
              {
                  text: 'Save',
                  handler: data => {
                      let jsondata = JSON.parse(JSON.stringify(data));
                      this.opassword = jsondata['opassword'];
		      this.npassword = jsondata['npassword'];
                      let myheaders = new HttpHeaders({ });
                      this.http.post("http://localhost:3000/viewprofile", {key: 'PASSWORD',old_password:this.opassword, new_password: this.npassword},{withCredentials: true, responseType:'text'})
                          .subscribe((data)=>{
                              if (data == "SAVED") {
                                  this.editCtrl.create({
                                      message:"Income has been changed!"
                                  });
                              }
                              else{
                                  this.editCtrl.create({
                                      message:"Change failed!"
                                  });
                              }
                          })
                      console.log('Saved clicked');
                  }
              }
          ]
      });
      edit.present();
  }
  logout(){
    let myheader = new HttpHeaders();
    this.http.get("http://localhost:3000/logout",{headers:myheader, withCredentials:true});
    var nav = this.app.getRootNav();
    nav.setRoot(LoginPage);
  }
  deleteProfile(){
    let myheader = new HttpHeaders();
    this.http.get("http://localhost:3000/deleteProfile", {withCredentials:true,responseType:'text'})
      .subscribe((data) => {
        if (data == "SUCCESS") {
          this.presentToast("Account Deleted");
          this.navCtrl.push(LoginPage);
        } else if (data == "FAILED") {
          this.presentToast("Wrong data!");
        }
      }, error => {
        console.log(error);
        this.presentToast("Error connecting to backend server");
      });
  }
  ngOnInit(){
      let myheader = new HttpHeaders();
      this.http.get("http://localhost:3000/viewprofile",{headers: myheader, withCredentials:true}).subscribe(data=>{
          let jsond = data[0];
          this.username=jsond['USER_NAME'].toString();
	  this.headerImage = 'http://localhost:3000/profileImages/' + this.username + ".jpeg";
          this.password=jsond['PASSWORD'].toString();
          this.firstname=jsond['FIRST_NAME'].toString();
          if (jsond['LAST_NAME']){
              this.lastname=jsond['LAST_NAME'].toString();
          }
          else {
              this.lastname="-";
          }
          if (jsond["BIRTH_DAY"]){
              this.dob=jsond["BD"].toString();
          }
          else {
              this.dob="-";
          }
          // this.age=jsond["AGE"].toString();
          if (jsond["SEX"]){
              this.genderlist=jsond["SEX"].toString();
          }
          else {
              this.genderlist="-";
          }
          if (jsond['INCOME']){
              this.income=jsond['INCOME'].toString();
          }
          else {
              this.income="-";
          }
      });

  }
}


import { NgModule } from '@angular/core';
import { IonicPageModule } from 'ionic-angular';
import { ItemPage } from './item';
import { CategoryPageModule } from '../category/category.module';
import {CategoryPage} from "../category/category";

@NgModule({
  declarations: [
    ItemPage,
  ],
  imports: [
    IonicPageModule.forChild(ItemPage),
  ],
  exports: [
    ItemPage,
  ]
})
export class ItemPageModule {}

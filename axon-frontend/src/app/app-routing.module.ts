import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { SettingsComponent } from './settings';
import {AuthGuard} from '@app/core/auth/auth.guard'

const routes: Routes = [
  {
    path: '',
    redirectTo: 'bpm',
    pathMatch: 'full'
  },
  {
    path: 'bpm',
    loadChildren: 'app/bpm/bpm.module#BpmModule',
    canActivate: [AuthGuard]
  },
  {
    path: 'org-structure',
    loadChildren: 'app/org-structure/org-structure.module#OrgStructureModule',
    canActivate: [AuthGuard]
  },
  {
    path: 'projects',
    loadChildren: 'app/projects/projects.module#ProjectsModule',
    canActivate: [AuthGuard]
  },
  {
    path: 'config',
    loadChildren: 'app/config/config.module#ConfigModule',
    canActivate: [AuthGuard]
  },
  {
    path: 'admin',
    loadChildren: 'app/admin/admin.module#AdminModule',
    canActivate: [AuthGuard]
  },
  {
    path: 'settings',
    component: SettingsComponent,
    data: { title: 'axon.menu.settings' },
    canActivate: [AuthGuard]
  },
  {
    path: 'examples',
    loadChildren: 'app/examples/examples.module#ExamplesModule',
    canActivate: [AuthGuard]
  },
  {
    path: '**',
    redirectTo: 'bpm'
  }
];

@NgModule({
  // useHash supports github.io demo page, remove in your app
  imports: [
    RouterModule.forRoot(routes, {
      enableTracing: false,
      scrollPositionRestoration: 'enabled'
    })
  ],
  exports: [RouterModule]
})
export class AppRoutingModule {}
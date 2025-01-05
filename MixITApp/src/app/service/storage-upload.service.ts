import {Injectable} from "@angular/core";
import {AngularFireStorage} from "@angular/fire/compat/storage";
import {catchError, finalize, from, Observable, throwError} from "rxjs";

@Injectable({
  providedIn: "root"
})
export class StorageUploadService{
  constructor(private storage: AngularFireStorage) {}
  /**
   * Zwraca Observable z procentowym postępem przesyłania pliku.
   * @param file Plik do przesłania
   * @param filePath Ścieżka, gdzie plik zostanie zapisany
   * @returns Observable z procentowym postępem
   */
  uploadFile(file: File, filePath: string): Observable<string>{
    const fileRef = this.storage.ref(filePath);
    const uploadTask = this.storage.upload(filePath, file);

    return new Observable((observer) => {
      uploadTask.snapshotChanges().pipe(
        finalize(() => {
          fileRef.getDownloadURL().subscribe((url) => {
            observer.next(url);
            observer.complete();
          });
        })
      ).subscribe();
    });
  }
  /**
   * Usuwa plik z Firebase Storage.
   * @param fileUrl URL pliku do usunięcia
   * @returns Observable po zakończeniu usuwania
   */
  deleteFile(fileUrl: string): Observable<void> {
    if (!fileUrl || !fileUrl.includes('firebase')) {
      console.error('Invalid Firebase Storage URL');
      return throwError(() => new Error('Invalid Firebase Storage URL'));
    }

    try {
      const storageRef = this.storage.refFromURL(fileUrl);
      return from(storageRef.delete()).pipe(
        catchError(error => {
          console.error('Error deleting file:', error);
          return throwError(() => error);
        })
      );
    } catch (error) {
      console.error('Error creating storage reference:', error);
      return throwError(() => error);
    }
  }
}

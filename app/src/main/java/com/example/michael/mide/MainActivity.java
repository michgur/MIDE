package com.example.michael.mide;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity
{
    private static Repository rep;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET }, 1);

        FloatingActionButton clone = findViewById(R.id.fab), delete = findViewById(R.id.fab2), push = findViewById(R.id.fab3);
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Test";
        System.out.println(path);
        clone.setOnClickListener(view -> {
            try { new CloneRep().execute("https://github.com/michgur/Test.git", path).get(); }
            catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
        });
        delete.setOnClickListener(view -> deleteProject(new File(path)));
        push.setOnClickListener(view -> {
            try { new PushRep().doInBackground("https://github.com/michgur/Test.git"); }
            catch (Exception e) { e.printStackTrace(); }
        });
    }

    private static void deleteProject(File file) {
        if (file.isDirectory())
            for (File child : file.listFiles()) deleteProject(child);
        file.delete();
    }

    private static class CloneRep extends AsyncTask<String, Void, Boolean> {
        @Override protected Boolean doInBackground(String... params) {
            String uri = params[0], directory = params[1];
            try { rep = Git.cloneRepository().setURI(uri).setDirectory(new File(directory)).call().getRepository(); }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            } return true;
        }
    }

    private class PushRep extends AsyncTask<String, Void, Boolean>
    {
        @Override protected Boolean doInBackground(String... params) {
            String uri = params[0];
            final boolean[] success = {false};

            View view = getLayoutInflater().inflate(R.layout.request_credentials, null);
            new AlertDialog.Builder(MainActivity.this).
                    setView(view)
                    .setPositiveButton("OK", (dialog, id) -> {
                        try {
                            EditText un = view.findViewById(R.id.editText), pw = view.findViewById(R.id.editText2);
                            Git git;
                            if (rep == null) {
                                git = Git.open(new File("/storage/emulated/0/Download/Test/.git"));
                                rep = git.getRepository();
                            }
                            else git = new Git(rep);
                            git.add().addFilepattern("02.jpg").call();
                            git.commit().setMessage("ADDED THOMAS!!!").call();
                            git.push().setRemote(uri).setCredentialsProvider(
                                    new UsernamePasswordCredentialsProvider(un.getText().toString(), pw.getText().toString())).call();
                            success[0] = true;
                        } catch (Exception e) { e.printStackTrace(); }
                    }).setNegativeButton("Cancel", (dialog, id) -> dialog.cancel()).show();
            return success[0];
        }
    }
}

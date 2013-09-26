package com.teddyhyde;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.GistService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: xrdawson
 * Date: 4/2/13
 * Time: 8:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class ThGitClient {

    public static String GetFile( String authToken, String repoName, String login, String fileSha ) throws IOException {
        RepositoryService repositoryService = new RepositoryService();
        repositoryService.getClient().setOAuth2Token(authToken);
        Repository repo = repositoryService.getRepository(login, repoName);
        DataService ds = new DataService();
        ds.getClient().setOAuth2Token(authToken);
        Blob blob = ds.getBlob(repo, fileSha);
        String theMarkdown64 = blob.getContent();
        String encoding = blob.getEncoding();
        byte[] decoded = Base64.decodeBase64(theMarkdown64.getBytes());
        return new String( decoded );
    }

    public static String SaveGist( String authToken, String contents, String description, String filename ) {
        GistFile file = new GistFile();
        file.setContent(contents);
        Gist gist = new Gist();
        gist.setDescription( description );
        gist.setFiles(Collections.singletonMap(filename, file));
        GistService service = new GistService();
        service.getClient().setOAuth2Token( authToken );
        try {
            gist = service.createGist(gist); //returns the created gist
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gist.getHtmlUrl();
    }

    public static String SaveFile( String authToken, String repoName, String login, String contentsBase64, String filename, String commitMessage ) {

        String newSha = null;
        boolean rv = true;

        try {
            // Thank you!
            // https://gist.github.com/Detelca/2337731

            // create needed services
            RepositoryService repositoryService = new RepositoryService();
            repositoryService.getClient().setOAuth2Token(authToken);
            CommitService commitService = new CommitService();
            commitService.getClient().setOAuth2Token(authToken);
            DataService dataService = new DataService();
            dataService.getClient().setOAuth2Token(authToken);

            // get some sha's from current state in git
            Repository repository =  repositoryService.getRepository(login, repoName);
            List<RepositoryBranch> branches = repositoryService.getBranches(repository);
            RepositoryBranch theBranch = null;
            RepositoryBranch master = null;
            // Iterate over the branches and find gh-pages or master
            for( RepositoryBranch i : branches ) {
                String theName = i.getName().toString();
                if( theName.equalsIgnoreCase("gh-pages") ) {
                    theBranch = i;
                }
                else if( theName.equalsIgnoreCase("master") ) {
                    master = i;
                }
            }
            if( null == theBranch ) {
                theBranch = master;
            }

            String baseCommitSha = theBranch.getCommit().getSha();

            // create new blob with data

            Random random = new Random();
            Blob blob = new Blob();
            blob.setContent(contentsBase64);
            blob.setEncoding(Blob.ENCODING_BASE64);
            String blob_sha = dataService.createBlob(repository, blob);
            Tree baseTree = dataService.getTree(repository, baseCommitSha);

            // create new tree entry
            TreeEntry treeEntry = new TreeEntry();
            treeEntry.setPath(filename);
            treeEntry.setMode(TreeEntry.MODE_BLOB);
            treeEntry.setType(TreeEntry.TYPE_BLOB);

            treeEntry.setSha(blob_sha);
            treeEntry.setSize(blob.getContent().length());
            Collection<TreeEntry> entries = new ArrayList<TreeEntry>();
            entries.add(treeEntry);
            Tree newTree = dataService.createTree(repository, entries, baseTree.getSha());

            // create commit
            Commit commit = new Commit();
            commit.setMessage( commitMessage );
            commit.setTree(newTree);
            List<Commit> listOfCommits = new ArrayList<Commit>();
            listOfCommits.add(new Commit().setSha(baseCommitSha));
            // listOfCommits.containsAll(base_commit.getParents());
            commit.setParents(listOfCommits);
            // commit.setSha(base_commit.getSha());
            Commit newCommit = dataService.createCommit(repository, commit);

            // create resource
            TypedResource commitResource = new TypedResource();
            commitResource.setSha(newCommit.getSha());
            commitResource.setType(TypedResource.TYPE_COMMIT);
            commitResource.setUrl(newCommit.getUrl());

            // get master reference and update it
            Reference reference = dataService.getReference(repository, "heads/" + theBranch.getName() );
            reference.setObject(commitResource);
            Reference response = dataService.editReference(repository, reference, true) ;
            newSha = treeEntry.getSha();
        }
        catch( IOException ieo ) {
            ieo.printStackTrace();
        }

        return newSha;
    }

}

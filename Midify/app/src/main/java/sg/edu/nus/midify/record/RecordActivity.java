package sg.edu.nus.midify.record;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import sg.edu.nus.POJOs.MidiPOJO;
import sg.edu.nus.helper.http.ConnectionHelper;
import sg.edu.nus.helper.Constant;
import sg.edu.nus.helper.http.MidifyRestClient;
import sg.edu.nus.helper.persistence.PersistenceHelper;
import sg.edu.nus.midi.MidiFile;
import sg.edu.nus.midi.MidiTrack;
import sg.edu.nus.midi.event.meta.Tempo;
import sg.edu.nus.midi.event.meta.TimeSignature;
import sg.edu.nus.midify.R;


public class RecordActivity extends Activity implements InitTaskDelegate, RecordTaskDelegate, ConvertTaskDelegate {

    private static final String RECORD_TAG = "record";
    private static final String STOP_RECORD_TAG = "stop";
    private static final int DEFAULT_TEMPO = 228;
    private static final int DEFAULT_CHANNEL = 0;

    // UI Controls
    private TextView statusTextView;
    private Button recordButton;

    private Recorder recorderProcess;
    private List<Note> midiNotes;
    private PcmToWavConverter pcmToWavConverter;
    private WavToMidiConverter wavToMidiConverter;

    // Persistence Data
    private SharedPreferences midiPreferences;
    private List<MidiPOJO> midiList;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        // UI Control Assignment
        statusTextView = (TextView) findViewById(R.id.status_text_view);
        recordButton = (Button) findViewById(R.id.record_button);

        // Loading Preferences
        midiList = PersistenceHelper.getMidiList(this);

        // Background initializing task
        InitTask initTask = new InitTask(this);
        initTask.execute();
    }

    public void onRecordButtonClick (View view) {
        if (recordButton.getText().toString().toLowerCase().equals(RECORD_TAG)) {
            RecordTask task = new RecordTask(this);
            task.execute();
            recordButton.setText(STOP_RECORD_TAG);
        } else if (recordButton.getText().toString().toLowerCase().equals(STOP_RECORD_TAG)) {
            if (recorderProcess != null) {
                recorderProcess.setRecording(false);
            }
            recordButton.setEnabled(false);
        }
    }


    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    //=============================================================================================
    // INIT TASK DELEGATE
    //=============================================================================================

    /**
     * Load the JNI Native Libs
     */
    @Override
    public void loadNativeLibs() {
        try {
            System.loadLibrary("atm");
        } catch (Exception ex) {
            Log.e(Constant.JNI_TAG, "Failed to load native library: " + ex);
        }
    }

    /**
     * Initialize the two necessary converters
     */
    @Override
    public void initializeConverters() {
        // Initialize the PCM to WAV converter
        pcmToWavConverter = new PcmToWavConverter(Constant.DEFAULT_PCM_FILE_PATH,
                                                   Constant.DEFAULT_WAV_FILE_PATH);

        // Initialize the WAV to MIDI converter
        wavToMidiConverter = new WavToMidiConverter(Constant.DEFAULT_WAV_FILE_PATH);
    }

    //=============================================================================================
    // RECORD TASK DELEGATE
    //=============================================================================================
    @Override
    public void setRecorder(Recorder recorder) {
        recorderProcess = recorder;
    }

    @Override
    public void convertPCMToMidi() {
        ConvertTask task = new ConvertTask(this);
        task.execute();
    }

    //=============================================================================================
    // CONVERT TASK DELEGATE
    //=============================================================================================
    @Override
    public void convertPcmToWav() {
        int bitsPerSampleInt = AudioFormat.ENCODING_PCM_16BIT;
        int bitsPerSample = 16;
        switch (bitsPerSampleInt) {
            case AudioFormat.ENCODING_DEFAULT:
                bitsPerSample = 16;
                break;
            case AudioFormat.ENCODING_PCM_8BIT:
                bitsPerSample = 8;
                break;
            case AudioFormat.ENCODING_PCM_16BIT:
                bitsPerSample = 16;
                break;

        }
        pcmToWavConverter.setBitPerSample(bitsPerSample);
        pcmToWavConverter.setChannels(1);
        pcmToWavConverter.setSamplerate(11025);
        pcmToWavConverter.convertPcm2wav();
    }

    @Override
    public void convertWavToMidi() {
        int bitsPerSampleInt = AudioFormat.ENCODING_PCM_16BIT;
        int bitsPerSample = 16;
        switch (bitsPerSampleInt) {
            case AudioFormat.ENCODING_DEFAULT:
                bitsPerSample = 16;
                break;
            case AudioFormat.ENCODING_PCM_8BIT:
                bitsPerSample = 8;
                break;
            case AudioFormat.ENCODING_PCM_16BIT:
                bitsPerSample = 16;
                break;

        }

        // Set Wav Params
        wavToMidiConverter.setBitspersample(bitsPerSample);
        wavToMidiConverter.setChannels(1);
        wavToMidiConverter.setSamplerate(11025);

        //Set Engine Params
        wavToMidiConverter.setBuffer_size(512);
        wavToMidiConverter.setOverlap_size(256);
        wavToMidiConverter.setSilence(-90);
        wavToMidiConverter.setThreshold(0.30f);
        wavToMidiConverter.setType_onset("kl");
        wavToMidiConverter.setType_onset2("complex");
        wavToMidiConverter.setType_pitch("yinfft");
        wavToMidiConverter.setAveraging((float) 1.0);

        // Start converting
        wavToMidiConverter.wav2midiNotes();
    }

    @Override
    public void populateMidiNotes() {
        this.midiNotes = wavToMidiConverter.getNotes();
        for (int i = 0; i < midiNotes.size(); i++) {
            Note note = midiNotes.get(i);
            if (note == null) {
                midiNotes.remove(i--);
            }
        }
        new MaterialDialog.Builder(this)
                .title(R.string.dialog_midi_name_input_title)
                .content(R.string.dialog_midi_name_input_content)
                .input(R.string.dialog_midi_name_input_hint,
                        R.string.dialog_midi_name_input_prefill,
                        new MaterialDialog.InputCallback() {

                    @Override
                    public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                        materialDialog.dismiss();
                        createMidiFile(charSequence.toString());
                    }

                }).show();

    }

    private void createMidiFile(String midiFileName) {

        // Create MIDI tracks
        MidiTrack tempoTrack = new MidiTrack();
        MidiTrack noteTrack = new MidiTrack();

        // First track is for tempo map
        TimeSignature ts = new TimeSignature();
        ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);
        Tempo t = new Tempo();
        t.setBpm(DEFAULT_TEMPO);
        tempoTrack.insertEvent(ts);
        tempoTrack.insertEvent(t);

        // Second track is for note map
        long totalDuration = 0;
        for (int i = 0; i < midiNotes.size(); i++) {
            Note note = midiNotes.get(i);
            int channel = DEFAULT_CHANNEL;
            int pitch = note.note;
            int velocity = note.vel;
            long duration = (long) note.time;
            totalDuration += duration;
            long tick = i * 480;

            noteTrack.insertNote(channel, pitch, velocity, tick, duration);
        }

        // Create MIDI File
        ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
        tracks.add(tempoTrack);
        tracks.add(noteTrack);

        MidiFile midiFile = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);
        String filePath = Constant.BASE_FILE_DIR + midiFileName
                        + String.valueOf(System.currentTimeMillis() / 1000 + ".mid");
        File output = new File(filePath);
        try {
            midiFile.writeToFile(output);
        } catch(IOException e) {
            Log.e(Constant.RECORD_TAG, "Cannot write the MIDI file output");
        }

        String facebookUserId = PersistenceHelper.getFacebookUserId(this);

        MidiPOJO newMidi = MidiPOJO.createLocalMidiWithoutId(midiFileName, filePath, facebookUserId);
        midiList.add(newMidi);
        PersistenceHelper.saveMidiList(this, midiList);
        if (ConnectionHelper.checkNetworkConnection(this)) {
            try {
                MidifyRestClient.instance()
                        .uploadMidi(filePath, midiFileName, new Callback<MidiPOJO>() {
                    @Override
                    public void success(MidiPOJO midiPOJO, Response response) {
                        System.out.println("CC");
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        System.out.println("URL: " + error.getUrl());
                    }
                });
            } catch (IOException e) {
                Log.e(Constant.RECORD_TAG, "Cannot upload due to invalid MIDI file path");
            }
        }
    }
}

package me.timesquared;

import com.xuggle.mediatool.IMediaViewer;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) {
		// the clock time of the next frame
		long nextFrameTime = 0;
		
		// video parameters
		final int videoStreamIndex = 0;
		final int videoStreamId = 0;
		final long frameRate = DEFAULT_TIME_UNIT.convert(500, MILLISECONDS);
		final int width = 320;
		final int height = 200;
		
		// audio parameters (unused)
//		final int audioStreamIndex = 1;
//		final int audioStreamId = 0;
//		final int channelCount = 1;
//		final int sampleRate = 44100; // Hz
//		final int sampleCount = 1000;
		
		try {
			final IMediaWriter writer = ToolFactory.makeWriter("test.mov");
			
			writer.addListener(
					ToolFactory.makeViewer(
							IMediaViewer.Mode.VIDEO_ONLY,
							true,
							javax.swing.WindowConstants.EXIT_ON_CLOSE
					)
			);
			
			writer.addVideoStream(videoStreamIndex, videoStreamId, width, height);
			//writer.addAudioStream(audioStreamIndex, audioStreamId, channelCount, sampleRate);
			
			File dir;
			
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			int returnVal = fileChooser.showOpenDialog(null);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				dir = fileChooser.getSelectedFile();
			} else {
				JOptionPane.showMessageDialog(
						null,
						"""
								You must choose a directory that contains some image files
								to render into a finalized video file.
								""",
						"No directory selected",
						JOptionPane.ERROR_MESSAGE
				);
				
				return;
			}
			
			if (dir.listFiles() == null) {
				JOptionPane.showMessageDialog(
						null,
						"""
								The directory you choose must contain image files to convert.
								""",
						"Directory is empty",
						JOptionPane.ERROR_MESSAGE
				);
				
				return;
			}
			
			// intellij being annoying :(
			for (File f : Objects.requireNonNull(dir.listFiles())) {
				BufferedImage frame = ImageIO.read(f);
				writer.encodeVideo(videoStreamIndex, frame, nextFrameTime, DEFAULT_TIME_UNIT);
				nextFrameTime += frameRate;

				// for audio, currently unused
//				short[] samples = new short[];
//				writer.encodeAudio(audioStreamIndex, samples, clock, DEFAULT_TIME_UNIT);
//				totalSampleCount += sampleCount;
			}
			writer.close();
			
		} catch (Exception e) {
			log.error("", e);
		}
	}
}

import Foundation
import AVFoundation
import AppKit

let args = CommandLine.arguments
if args[1] == "join" {
    let composition = AVMutableComposition()
    let track = composition.addMutableTrack(withMediaType: .video, preferredTrackID: kCMPersistentTrackID_Invalid)!
    var cursor = CMTime.zero
    for path in args.dropFirst(3) {
        let asset = AVURLAsset(url: URL(fileURLWithPath: path))
        let source = asset.tracks(withMediaType: .video).first!
        try track.insertTimeRange(CMTimeRange(start: .zero, duration: asset.duration), of: source, at: cursor)
        track.preferredTransform = source.preferredTransform
        cursor = CMTimeAdd(cursor, asset.duration)
    }
    let output = URL(fileURLWithPath: args[2])
    try? FileManager.default.removeItem(at: output)
    let exporter = AVAssetExportSession(asset: composition, presetName: AVAssetExportPresetPassthrough)!
    exporter.outputURL = output
    exporter.outputFileType = .mp4
    let done = DispatchSemaphore(value: 0)
    exporter.exportAsynchronously { done.signal() }
    done.wait()
    guard exporter.status == .completed else { fatalError("Video export failed: \(String(describing: exporter.error))") }
} else {
    let directory = URL(fileURLWithPath: args[2])
    try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
    for path in args.dropFirst(3) {
        let asset = AVURLAsset(url: URL(fileURLWithPath: path))
        let generator = AVAssetImageGenerator(asset: asset)
        generator.appliesPreferredTrackTransform = true
        generator.requestedTimeToleranceBefore = .zero
        generator.requestedTimeToleranceAfter = .zero
        let duration = CMTimeGetSeconds(asset.duration)
        print("\(URL(fileURLWithPath: path).lastPathComponent): \(duration) seconds")
        guard duration > 1 else { fatalError("Recording is empty") }
        for index in 0..<8 {
            let time = CMTime(seconds: duration * (Double(index) + 0.5) / 8, preferredTimescale: 600)
            let cg = try generator.copyCGImage(at: time, actualTime: nil)
            let data = NSBitmapImageRep(cgImage: cg).representation(using: .png, properties: [:])!
            let name = URL(fileURLWithPath: path).deletingPathExtension().lastPathComponent
            try data.write(to: directory.appendingPathComponent("\(name)-\(index).png"))
        }
    }
}

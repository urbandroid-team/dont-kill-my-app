module Jekyll
  module AbsolutizeImgUrls
    def absolutize_img_urls(text)
      text.gsub(/src="\/assets\/img\//, 'src="https://dontkillmyapp.com/assets/img/')
    end
  end
end

Liquid::Template.register_filter(Jekyll::AbsolutizeImgUrls)